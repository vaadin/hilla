package com.vaadin.hilla.engine;

import java.util.Arrays;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public interface CustomEngineConfiguration {
    default BrowserCallableFinder getBrowserCallableFinder(
            BrowserCallableFinder defaultFinder) {
        return defaultFinder;
    }

    static CustomEngineConfiguration load() {
        return pick(ServiceLoader.load(CustomEngineConfiguration.class).stream()
                .map(ServiceLoader.Provider::get)
                .toArray(CustomEngineConfiguration[]::new));
    }

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
