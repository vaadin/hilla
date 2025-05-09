package com.vaadin.hilla.engine;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.server.frontend.scanner.ClassFinder;

class LookupBrowserCallableFinder {

    static List<Class<?>> findEndpointClasses(ClassFinder classFinder,
            EngineConfiguration engineConfiguration) {
        var annotations = engineConfiguration.getEndpointAnnotations();

        return annotations.stream()
                .map(engineConfiguration.getClassFinder()::getAnnotatedClasses)
                .flatMap(Set::stream).distinct()
                .collect(Collectors.toMap(
                        clazz -> findEndpointName(annotations, clazz),
                        clazz -> clazz, (existing, duplicate) -> {
                            throw new IllegalStateException(
                                    "Duplicate key found: " + findEndpointName(
                                            annotations, existing));
                        }))
                .values().stream().collect(Collectors.toList());
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
                    }
                    return name == null || name.isEmpty()
                            ? clazz.getSimpleName()
                            : name;
                }).orElse(clazz.getSimpleName());
    }
}
