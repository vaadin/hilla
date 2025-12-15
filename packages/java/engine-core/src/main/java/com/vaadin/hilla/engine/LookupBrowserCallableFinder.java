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
package com.vaadin.hilla.engine;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Finds browser callables (endpoints) in a running Hilla application by
 * inspecting the classes annotated with the endpoint annotations.
 */
public class LookupBrowserCallableFinder {

    /**
     * Implements the {@link BrowserCallableFinder} interface.
     *
     * @param engineConfiguration
     *            the engine configuration
     * @return the list of classes annotated with the configured annotations
     * @throws BrowserCallableFinderException
     *             if an error occurs while finding the browser callables
     */
    public static List<Class<?>> find(
            EngineAutoConfiguration engineConfiguration)
            throws BrowserCallableFinderException {
        var annotations = engineConfiguration.getEndpointAnnotations();

        try {
            return annotations.stream()
                    .map(engineConfiguration
                            .getClassFinder()::getAnnotatedClasses)
                    .flatMap(Set::stream).distinct()
                    .collect(Collectors.toMap(
                            clazz -> findEndpointName(annotations, clazz),
                            clazz -> clazz, (existing, duplicate) -> {
                                throw new InternalException(
                                        "Duplicate key found: "
                                                + findEndpointName(annotations,
                                                        existing));
                            }))
                    .values().stream().collect(Collectors.toList());
        } catch (Exception e) {
            if (e instanceof InternalException) {
                throw new BrowserCallableFinderException(e.getMessage());
            }
            throw new BrowserCallableFinderException(e);
        }
    }

    private static class InternalException extends RuntimeException {
        public InternalException(String message) {
            super(message);
        }
    }

    private static String findEndpointName(
            List<Class<? extends Annotation>> annotations, Class<?> clazz) {
        return annotations.stream()
                .filter(annotation -> clazz.isAnnotationPresent(annotation))
                .findAny().map(annotation -> {
                    String name = null;
                    try {
                        name = (String) annotation.getMethod("value")
                                .invoke(clazz.getAnnotation(annotation));
                    } catch (Exception e) {
                        // ok, annotation does not support value
                    }
                    return name == null || name.isEmpty()
                            ? clazz.getSimpleName()
                            : name;
                }).orElse(clazz.getSimpleName());
    }
}
