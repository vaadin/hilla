package com.vaadin.hilla.startup;

import com.vaadin.flow.server.frontend.EndpointUsageDetector;
import com.vaadin.flow.server.frontend.Options;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.Endpoint;

/**
 * Implementation of EndpointUsageDetector which determines if the endpoint
 * generator task needs to be run.
 */
public class EndpointUsageDetectorImpl implements EndpointUsageDetector {

    @Override
    public boolean areEndpointsUsed(Options options) {
        ClassFinder classFinder = options.getClassFinder();
        return !classFinder.getAnnotatedClasses(Endpoint.class).isEmpty()
                || !classFinder.getAnnotatedClasses(BrowserCallable.class)
                        .isEmpty();
    }

}
