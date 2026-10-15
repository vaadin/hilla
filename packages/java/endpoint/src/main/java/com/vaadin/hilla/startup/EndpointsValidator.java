/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla.startup;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HandlesTypes;
import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.server.frontend.scanner.ClassFinder;
import com.vaadin.flow.server.frontend.scanner.ClassFinder.DefaultClassFinder;
import com.vaadin.flow.server.startup.ClassLoaderAwareServletContainerInitializer;

import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.Endpoint;

/**
 * Validation class that is run during servlet container initialization which
 * checks that application is running with the appropriate spring dependencies
 * when there are {@link Endpoint} annotations.
 *
 * @since 3.0
 */
@HandlesTypes({ Endpoint.class, BrowserCallable.class })
public class EndpointsValidator
        implements ClassLoaderAwareServletContainerInitializer, Serializable {

    private String classToCheck = "org.springframework.boot.autoconfigure.jackson.JacksonProperties";

    @Override
    public void process(Set<Class<?>> classSet, ServletContext servletContext)
            throws ServletException {

        if (classSet == null) {
            // This case happens when initializing in a CDI environment.
            //
            // We cannot check anything here to give a message.
            // Continue with the initialization, java will throw
            // the proper exception if application tries to use
            // an endpoint and dependencies are not added to the project.
            return;
        }

        ClassFinder finder = new DefaultClassFinder(classSet);
        Set<Class<?>> endpoints = finder.getAnnotatedClasses(Endpoint.class);
        endpoints.addAll(finder.getAnnotatedClasses(BrowserCallable.class));
        if (!endpoints.isEmpty()) {
            try {
                finder.loadClass(classToCheck);
            } catch (ClassNotFoundException e) {
                throw new ServletException(
                        "ERROR: Vaadin endpoints only work for Spring "
                                + "enabled projects.\n"
                                + "This is not a spring application but there are Vaadin endpoints in these classes: "
                                + endpoints.stream().map(Class::getName)
                                        .collect(
                                                Collectors.joining("\n    - ")),
                        e);
            }
        }
    }

    void setClassToCheck(String className) {
        classToCheck = className;
    }
}
