package com.vaadin.hilla.springnative;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.router.MenuData;
import com.vaadin.flow.server.menu.AvailableViewInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.vaadin.hilla.OpenAPIUtil;
import com.vaadin.hilla.engine.EngineAutoConfiguration;
import com.vaadin.hilla.push.PushEndpoint;
import com.vaadin.hilla.push.messages.fromclient.AbstractServerMessage;
import com.vaadin.hilla.push.messages.toclient.AbstractClientMessage;

/**
 * Registers runtime hints for Spring 3 native support for Hilla.
 */
public class HillaHintsRegistrar implements RuntimeHintsRegistrar {

    private static final String openApiResourceName = "/"
            + EngineAutoConfiguration.OPEN_API_PATH;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        registerEndpointTypes(hints);

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

    private void registerEndpointTypes(RuntimeHints hints) {
        try {
            var resource = getClass().getResource(openApiResourceName);
            if (resource == null) {
                logger.error("Resource {} is not available",
                        openApiResourceName);
                return;
            } else {
                logger.info(
                        "Resource {} is being used for registering endpoint types",
                        openApiResourceName);
            }

            var reader = new BufferedReader(
                    new InputStreamReader(resource.openStream()));
            String openApiAsText = reader.lines()
                    .collect(Collectors.joining("\n"));
            Set<String> types = OpenAPIUtil.findOpenApiClasses(openApiAsText);
            for (String type : types) {
                hints.reflection().registerType(TypeReference.of(type),
                        MemberCategory.values());
            }
        } catch (IOException e) {
            logger.error("Error while scanning and registering endpoint types",
                    e);
        }
        hints.resources()
                .registerPattern(EngineAutoConfiguration.OPEN_API_PATH);
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
