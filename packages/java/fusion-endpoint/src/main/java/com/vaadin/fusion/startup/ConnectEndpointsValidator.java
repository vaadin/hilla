/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.fusion.startup;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.fusion.Endpoint;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;
import com.vaadin.flow.server.frontend.scanner.ClassFinder.DefaultClassFinder;
import com.vaadin.flow.server.startup.ClassLoaderAwareServletContainerInitializer;

/**
 * Validation class that is run during servlet container initialization which
 * checks that application is running with the appropriate spring dependencies
 * when there are {@link Endpoint} annotations.
 *
 * @since 3.0
 */
@HandlesTypes({ Endpoint.class })
public class ConnectEndpointsValidator
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
