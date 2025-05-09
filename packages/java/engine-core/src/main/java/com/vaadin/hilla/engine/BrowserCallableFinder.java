package com.vaadin.hilla.engine;

import java.util.List;

/**
 * Functional interface for finding browser-callable classes. Implementations of
 * this interface are responsible for locating and returning a list of endpoint
 * classes.
 */
@FunctionalInterface
public interface BrowserCallableFinder {
    /**
     * Finds and returns a list of browser-callable classes based on the
     * provided configuration.
     *
     * @param engineConfiguration
     *            The configuration to use for finding classes.
     * @return A list of classes that are browser-callable.
     */
    List<Class<?>> find(EngineConfiguration engineConfiguration);
}
