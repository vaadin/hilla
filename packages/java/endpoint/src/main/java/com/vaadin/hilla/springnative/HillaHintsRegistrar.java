/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.springnative;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.github.classgraph.ClassGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

import com.vaadin.flow.router.MenuData;
import com.vaadin.flow.server.menu.AvailableViewInfo;
import com.vaadin.hilla.engine.EngineAutoConfiguration;
import com.vaadin.hilla.engine.ParserProcessor;
import com.vaadin.hilla.generator.model.EndpointModel;
import com.vaadin.hilla.generator.model.EntityModel;
import com.vaadin.hilla.push.PushEndpoint;
import com.vaadin.hilla.push.messages.fromclient.AbstractServerMessage;
import com.vaadin.hilla.push.messages.toclient.AbstractClientMessage;

/**
 * Registers runtime hints for Spring 3 native support for Hilla.
 */
public class HillaHintsRegistrar implements RuntimeHintsRegistrar {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        try {
            registerEndpointTypes(hints, classLoader);
        } catch (RuntimeException e) {
            // The rest of the hints are worth registering even where the
            // classes of the endpoints cannot be walked
            logger.error("Unable to register the types of the endpoints", e);
        }

        hints.resources().registerPattern("file-routes.json");
        hints.reflection().registerType(MenuData.class,
                MemberCategory.values());
        hints.reflection().registerType(AvailableViewInfo.class,
                MemberCategory.values());

        hints.reflection().registerType(PushEndpoint.class,
                MemberCategory.values());

        List<Class<?>> pushMessageTypes = new ArrayList<>();
        pushMessageTypes.addAll(getMessageTypes(AbstractServerMessage.class));
        pushMessageTypes.addAll(getMessageTypes(AbstractClientMessage.class));
        for (Class<?> cls : pushMessageTypes) {
            hints.reflection().registerType(cls, MemberCategory.values());
        }
    }

    /**
     * Registers the classes the browser reaches: the browser callable ones and
     * the types their methods send and take, which are what the values are
     * serialized from and into at runtime.
     *
     * <p>
     * They are found by walking the classes the way the generator does, which
     * is what says which types an endpoint exposes.
     */
    private void registerEndpointTypes(RuntimeHints hints,
            ClassLoader classLoader) {
        var configuration = new EngineAutoConfiguration.Builder()
                .withDefaultAnnotations().build();

        registerEndpointTypes(hints, configuration,
                browserCallables(classLoader, configuration));
    }

    /**
     * Registers the types the given browser callable classes expose, which is
     * what walking them says.
     */
    void registerEndpointTypes(RuntimeHints hints,
            EngineAutoConfiguration configuration,
            List<Class<?>> browserCallables) {
        if (browserCallables.isEmpty()) {
            // An application without endpoints is unusual enough to say, and
            // it is also what a scan which found nothing looks like
            logger.warn("No browser callable class to register types for");
            return;
        }

        logger.info("Registering the types of {} browser callable classes",
                browserCallables.size());

        var generation = new ParserProcessor(configuration)
                .parse(browserCallables);

        Stream.concat(
                generation.endpoints().stream().map(EndpointModel::javaClass),
                generation.entities().stream().map(EntityModel::javaClass))
                .distinct().forEach(type -> hints.reflection().registerType(
                        TypeReference.of(type), MemberCategory.values()));
    }

    /**
     * The browser callable classes of the application, which are the ones
     * annotated as such on the classpath being built.
     *
     * <p>
     * They are looked for here rather than through the finder of the
     * configuration, which neither of the finders it offers can do while a
     * native image is being built: one asks the running application, which
     * there is none of, and the other starts a build of its own, which this is
     * already part of. An application which finds its endpoints its own way
     * therefore has to say so in a hint of its own as well.
     */
    List<Class<?>> browserCallables(ClassLoader classLoader,
            EngineAutoConfiguration configuration) {
        // The classes of the application are the ones the loader given here
        // has, which are not necessarily the ones the process was started with
        try (var scan = new ClassGraph().overrideClassLoaders(classLoader)
                .enableAnnotationInfo().enableClassInfo().scan()) {
            return configuration.getEndpointAnnotations().stream()
                    .map(Class::getName).map(scan::getClassesWithAnnotation)
                    .flatMap(classes -> classes.loadClasses().stream())
                    .distinct().toList();
        }
    }

    private Collection<Class<?>> getMessageTypes(Class<?> cls) {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(cls);
        JsonSubTypes subTypes = cls.getAnnotation(JsonSubTypes.class);
        for (JsonSubTypes.Type t : subTypes.value()) {
            classes.add(t.value());
        }
        return classes;
    }

}
