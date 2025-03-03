package com.vaadin.hilla.engine;

import java.util.List;
import java.util.Set;

import com.vaadin.flow.server.frontend.scanner.ClassFinder;

class LookupBrowserCallableFinder {

    static List<Class<?>> findEndpointClasses(ClassFinder classFinder,
            EngineConfiguration engineConfiguration) {
        return engineConfiguration.getEndpointAnnotations().stream()
                .map(classFinder::getAnnotatedClasses).flatMap(Set::stream)
                .distinct().filter(clazz -> {
                    var location = clazz.getProtectionDomain().getCodeSource()
                            .getLocation();
                    return location != null
                            && location.getProtocol().equals("file")
                            && location.getPath().endsWith("/");
                }).toList();
    }

}
