/*
 * Copyright 2000-2020 Vaadin Ltd.
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
package com.vaadin.flow.server.connect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

/**
 * Keeps track of registered endpoints.
 */
@Component
public class EndpointRegistry {
    /**
     * Tracks methods inside a given endpoint class.
     */
    public static class VaadinEndpointData {
        final Map<String, Method> methods = new HashMap<>();
        private final Object vaadinEndpointObject;

        private VaadinEndpointData(Object vaadinEndpointObject,
                Method... endpointMethods) {
            this.vaadinEndpointObject = vaadinEndpointObject;
            Stream.of(endpointMethods)
                    .filter(method -> method.getDeclaringClass() != Object.class
                            && !method.isBridge())
                    .forEach(method -> methods.put(
                            method.getName().toLowerCase(Locale.ENGLISH),
                            method));
        }

        /**
         * Finds a method with the given name.
         *
         * @param methodName
         *            the name to look for
         * @return An optional containing a method reference or an empty
         *         optional if no method was found
         */
        public Optional<Method> getMethod(String methodName) {
            return Optional.ofNullable(
                    methods.get(methodName.toLowerCase(Locale.ENGLISH)));
        }

        public Object getEndpointObject() {
            return vaadinEndpointObject;
        }
    }

    private EndpointNameChecker endpointNameChecker;
    private final Map<String, VaadinEndpointData> vaadinEndpoints = new HashMap<>();

    /**
     * Creates a new registry using the given name checker.
     * 
     * @param endpointNameChecker
     *            the endpoint name checker to verify custom Vaadin endpoint
     *            names
     */
    public EndpointRegistry(EndpointNameChecker endpointNameChecker) {
        this.endpointNameChecker = endpointNameChecker;
    }

    private static String getEndpointNameForClass(Class<?> beanType) {
        return Optional.ofNullable(beanType.getAnnotation(Endpoint.class))
                .map(Endpoint::value).filter(value -> !value.isEmpty())
                .orElse(beanType.getSimpleName());
    }

    void registerEndpoint(Object endpointBean) {
        // Check the bean type instead of the implementation type in
        // case of e.g. proxies
        Class<?> beanType = ClassUtils.getUserClass(endpointBean.getClass());
        String endpointName = getEndpointNameForClass(beanType);

        if (endpointName.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "A bean with type '%s' is annotated with '%s' "
                            + "annotation but is an anonymous class hence has no name. ",
                    beanType, Endpoint.class)
                    + String.format(
                            "Either modify the bean declaration so that it is not an "
                                    + "anonymous class or specify an endpoint "
                                    + "name in the '%s' annotation",
                            Endpoint.class));
        }
        String validationError = endpointNameChecker.check(endpointName);
        if (validationError != null) {
            throw new IllegalStateException(
                    String.format("Endpoint name '%s' is invalid, reason: '%s'",
                            endpointName, validationError));
        }

        Method[] endpointPublicMethods = beanType.getMethods();
        AccessibleObject.setAccessible(endpointPublicMethods, true);

        vaadinEndpoints.put(endpointName.toLowerCase(Locale.ENGLISH),
                new VaadinEndpointData(endpointBean, endpointPublicMethods));

    }

    VaadinEndpointData get(String endpointName) {
        return vaadinEndpoints.get(endpointName.toLowerCase(Locale.ENGLISH));
    }

}
