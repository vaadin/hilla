package com.vaadin.hilla.engine;

import java.util.List;

import com.vaadin.flow.server.ExecutionFailedException;

/**
 * Functional interface for finding browser-callable classes. Implementations of
 * this interface are responsible for locating and returning a list of endpoint
 * classes.
 */
@FunctionalInterface
public interface BrowserCallableFinder {
    /**
     * Finds and returns a list of browser-callable endpoint classes based on
     * the provided configuration.
     *
     * @param engineConfiguration
     *            The configuration to use for finding endpoint classes.
     * @return A list of endpoint classes that are browser-callable.
     * @throws ExecutionFailedException
     *             If something goes wrong during the lookup.
     */
    List<Class<?>> findEndpointClasses(EngineConfiguration engineConfiguration)
            throws ExecutionFailedException;
}
