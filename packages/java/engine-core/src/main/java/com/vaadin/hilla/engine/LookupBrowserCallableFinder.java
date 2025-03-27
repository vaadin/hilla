package com.vaadin.hilla.engine;

import java.util.List;
import java.util.Set;

import com.vaadin.flow.server.ExecutionFailedException;

public class LookupBrowserCallableFinder {

    public static List<Class<?>> findEndpointClasses(
            EngineConfiguration engineConfiguration)
            throws ExecutionFailedException {
        return engineConfiguration.getEndpointAnnotations().stream()
                .map(engineConfiguration.getClassFinder()::getAnnotatedClasses)
                .flatMap(Set::stream).distinct().toList();
    }

}
