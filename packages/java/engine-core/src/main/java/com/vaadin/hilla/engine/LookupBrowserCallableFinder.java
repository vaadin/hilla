package com.vaadin.hilla.engine;

import java.util.List;
import java.util.Set;

class LookupBrowserCallableFinder {

    static List<Class<?>> findEndpointClasses(
            EngineConfiguration engineConfiguration) {
        return engineConfiguration.getEndpointAnnotations().stream()
                .map(engineConfiguration.getClassFinder()::getAnnotatedClasses)
                .flatMap(Set::stream).distinct().toList();
    }

}
