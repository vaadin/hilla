package com.vaadin.hilla.engine;

import java.util.List;
import java.util.Set;

import com.vaadin.flow.server.frontend.scanner.ClassFinder;

class LookupBrowserCallableFinder {

    static List<Class<?>> findEndpointClasses(ClassFinder classFinder,
            EngineConfiguration engineConfiguration) {
        return engineConfiguration.getEndpointAnnotations().stream()
                .map(classFinder::getAnnotatedClasses).flatMap(Set::stream)
                .distinct().toList();
    }

}
