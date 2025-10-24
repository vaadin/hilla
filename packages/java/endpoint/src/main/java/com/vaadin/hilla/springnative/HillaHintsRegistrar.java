package com.vaadin.hilla.springnative;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.flow.router.MenuData;
import com.vaadin.flow.server.menu.AvailableViewInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.vaadin.hilla.push.PushEndpoint;
import com.vaadin.hilla.push.messages.fromclient.AbstractServerMessage;
import com.vaadin.hilla.push.messages.toclient.AbstractClientMessage;

/**
 * Registers runtime hints for Spring 3 native support for Hilla.
 *
 * Note: Endpoint type registration has been removed as OpenAPI is no longer
 * generated. Endpoint types should be automatically discovered by Spring's
 * component scanning.
 */
public class HillaHintsRegistrar implements RuntimeHintsRegistrar {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
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
