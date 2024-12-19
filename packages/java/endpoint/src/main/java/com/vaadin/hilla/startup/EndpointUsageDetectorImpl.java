package com.vaadin.hilla.startup;

import java.util.stream.Stream;

import com.vaadin.flow.server.frontend.EndpointUsageDetector;
import com.vaadin.flow.server.frontend.Options;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.InternalBrowserCallable;

/**
 * Implementation of EndpointUsageDetector which determines if the endpoint
 * generator task needs to be run.
 */
public class EndpointUsageDetectorImpl implements EndpointUsageDetector {

    @Override
    public boolean areEndpointsUsed(Options options) {
        ClassFinder classFinder = options.getClassFinder();

        return Stream.concat(
                classFinder.getAnnotatedClasses(Endpoint.class).stream(),
                classFinder.getAnnotatedClasses(BrowserCallable.class).stream())
                .anyMatch(annotatedClass -> !annotatedClass
                        .isAnnotationPresent(InternalBrowserCallable.class));
    }

}
