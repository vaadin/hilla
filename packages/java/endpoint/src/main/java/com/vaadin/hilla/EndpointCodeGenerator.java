/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.hilla;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.vaadin.flow.internal.FrontendUtils;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.startup.ApplicationConfiguration;
import com.vaadin.hilla.engine.EngineAutoConfiguration;
import com.vaadin.hilla.engine.ParserProcessor;
import com.vaadin.hilla.engine.TypeScriptProcessor;
import com.vaadin.hilla.generator.model.EndpointModel;
import com.vaadin.hilla.generator.model.EntityModel;
import com.vaadin.hilla.generator.model.Generation;

/**
 * Handles (re)generation of the TypeScript code.
 */
@Component
public class EndpointCodeGenerator {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(EndpointCodeGenerator.class);

    private final EndpointController endpointController;
    private final VaadinContext context;

    private ApplicationConfiguration configuration;
    private Set<String> classesUsedInEndpoints = null;
    private EngineAutoConfiguration engineConfiguration;

    /**
     * Creates the singleton.
     *
     * @param context
     *            the context the application is running in
     * @param endpointController
     *            a reference to the endpoint controller
     */
    public EndpointCodeGenerator(VaadinContext context,
            EndpointController endpointController) {
        this.endpointController = endpointController;
        this.context = context;
    }

    /**
     * Gets the singleton instance.
     */
    public static EndpointCodeGenerator getInstance() {
        return ApplicationContextProvider.getApplicationContext()
                .getBean(EndpointCodeGenerator.class);
    }

    /**
     * Re-generates the endpoint TypeScript and re-registers the endpoints in
     * Java.
     *
     * @param proposedNewBrowserCallables
     *            Some classes that might be new browser callables, for example
     *            coming from a hotswap event.
     */
    public void update(String... proposedNewBrowserCallables) {
        initIfNeeded();
        if (configuration.isProductionMode()) {
            throw new IllegalStateException(
                    "This method is not available in production mode");
        }

        ApplicationContextProvider.runOnContext(applicationContext -> {
            List<Class<?>> browserCallables = findBrowserCallables(
                    engineConfiguration, applicationContext);

            browserCallables = Stream.concat(browserCallables.stream(), Arrays
                    .stream(proposedNewBrowserCallables).map(className -> {
                        try {
                            Class<?> cls = Class.forName(className);
                            if (cls.getAnnotation(Endpoint.class) != null
                                    || cls.getAnnotation(
                                            BrowserCallable.class) != null) {
                                return cls;
                            }

                        } catch (ClassNotFoundException e) {
                            LOGGER.error("Unable to find class " + className,
                                    e);
                        }
                        return null;
                    })).filter(Objects::nonNull).distinct().toList();

            var generation = new ParserProcessor(engineConfiguration)
                    .parse(browserCallables);

            TypeScriptProcessor generator = new TypeScriptProcessor(
                    engineConfiguration);
            generator.process(generation);
            classesUsedInEndpoints = classesUsedIn(generation);
            this.endpointController.registerEndpoints();
        });
    }

    /**
     * Finds all beans in the application context that have a browser callable
     * annotation.
     *
     * @param engineConfiguration
     *            the engine configuration that provides the annotations to
     *            search for
     * @param applicationContext
     *            the application context to search for beans in
     * @return a list of classes that qualify as browser callables
     */
    public static List<Class<?>> findBrowserCallables(
            EngineAutoConfiguration engineConfiguration,
            ApplicationContext applicationContext) {
        return engineConfiguration.getEndpointAnnotations().stream()
                .map(applicationContext::getBeansWithAnnotation)
                .map(Map::values).flatMap(Collection::stream)
                // maps to original class when proxies are found
                // (also converts to class in all cases)
                .map(AopProxyUtils::ultimateTargetClass).distinct()
                .collect(Collectors.toList());
    }

    private void initIfNeeded() {
        if (configuration == null) {
            configuration = ApplicationConfiguration.get(context);

            engineConfiguration = new EngineAutoConfiguration.Builder()
                    .baseDir(configuration.getProjectFolder().toPath())
                    .buildDir(configuration.getBuildFolder())
                    .outputDir(
                            FrontendUtils
                                    .getFrontendGeneratedFolder(
                                            configuration.getFrontendFolder())
                                    .toPath())
                    .productionMode(false).withDefaultAnnotations().build();
        }
    }

    /**
     * The classes the TypeScript of the endpoints is written from: the browser
     * callable ones and the types their methods send and take. They are what a
     * change has to touch to be worth generating again for.
     *
     * @return the classes, or nothing where the browser callable classes have
     *         not been walked yet
     */
    public Optional<Set<String>> getClassesUsedInEndpoints() {
        if (classesUsedInEndpoints == null) {
            initIfNeeded();

            // Asked for rather than queued: the walk is only worth doing where
            // there is a context to find the browser callable classes in, and
            // queueing one for later would pile up a walk per question
            var applicationContext = ApplicationContextProvider
                    .getApplicationContext();

            if (applicationContext == null) {
                LOGGER.debug("There is no application context to find the"
                        + " browser callable classes in yet");

                return Optional.empty();
            }

            classesUsedInEndpoints = classesUsedIn(
                    new ParserProcessor(engineConfiguration)
                            .parse(findBrowserCallables(engineConfiguration,
                                    applicationContext)));
        }

        return Optional.ofNullable(classesUsedInEndpoints);
    }

    /**
     * The classes one generation is written from.
     */
    private static Set<String> classesUsedIn(Generation generation) {
        return Stream.concat(
                generation.endpoints().stream().map(EndpointModel::javaClass),
                generation.entities().stream().map(EntityModel::javaClass))
                .collect(Collectors.toSet());
    }
}
