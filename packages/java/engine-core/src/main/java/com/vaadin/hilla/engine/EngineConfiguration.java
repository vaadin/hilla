package com.vaadin.hilla.engine;

/**
 * Configuration for the generator engine. It exposes all properties that can be
 * overridden by an alternative implementation. All methods take the default
 * value as a parameter and return the default value if not overridden.
 */
public interface EngineConfiguration {
    /**
     * Returns the finder for browser-callable classes. This is used when
     * building the application for production.
     *
     * @param defaultFinder
     *            The default finder to use if no custom finder is provided.
     * @return The browser-callable finder to use.
     */
    default BrowserCallableFinder getBrowserCallableFinder(
            BrowserCallableFinder defaultFinder) {
        return defaultFinder;
    }

    /**
     * Returns the class loader to use for loading classes. This is used when
     * building the application for production.
     *
     * @param defaultClassLoader
     *            the default class loader
     * @return the class loader to use
     */
    default ClassLoader getClassLoader(ClassLoader defaultClassLoader) {
        return defaultClassLoader;
    }
}
