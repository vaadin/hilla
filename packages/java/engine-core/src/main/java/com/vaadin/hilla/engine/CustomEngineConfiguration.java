package com.vaadin.hilla.engine;

import java.util.Arrays;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Configuration for the generator engine. It exposes all properties that can be
 * overridden by an alternative implementation. All methods take the default
 * value as a parameter and return the default value if not overridden.
 *
 * @see #load() for details on how to override the default configuration
 */
public interface CustomEngineConfiguration {
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

    /**
     * Loads the configuration service using the Java ServiceLoader mechanism.
     */
    static CustomEngineConfiguration load() {
        return pick(ServiceLoader.load(CustomEngineConfiguration.class).stream()
                .map(ServiceLoader.Provider::get)
                .toArray(CustomEngineConfiguration[]::new));
    }

    /**
     * Picks the first configuration from the provided array. If there are no
     * configurations, a default one is returned. If there are multiple
     * configurations, an exception is thrown.
     *
     * @param configurations
     *            the configurations to pick from
     * @return the picked configuration
     */
    static CustomEngineConfiguration pick(
            CustomEngineConfiguration... configurations) {
        return switch (configurations.length) {
        case 0 -> new CustomEngineConfiguration() {
        };
        case 1 -> configurations[0];
        default -> throw new ConfigurationException(Arrays
                .stream(configurations)
                .map(config -> config.getClass().getName())
                .collect(Collectors.joining("\", \"",
                        "Multiple CustomEngineConfiguration implementations found: \"",
                        "\"")));
        };
    }
}
