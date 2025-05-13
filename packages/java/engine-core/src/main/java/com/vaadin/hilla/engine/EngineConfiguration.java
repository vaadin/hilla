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
     * Returns the node command to use for running the generator. Other parts of
     * the software can still use a different command.
     *
     * @param defaultNodeCommand
     *            the default node command
     * @return the node command to use
     */
    default String getNodeCommand(String defaultNodeCommand) {
        return defaultNodeCommand;
    }
}
