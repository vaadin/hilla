package com.vaadin.hilla.engine;

public final class ConfigurationException extends RuntimeException {
    ConfigurationException(String message) {
        super(message);
    }

    ConfigurationException(Throwable cause) {
        super(cause);
    }

    ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
