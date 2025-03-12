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
    List<Class<?>> findBrowserCallables() throws ExecutionFailedException;
}
