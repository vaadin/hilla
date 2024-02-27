package com.vaadin.hilla.maven;

import org.apache.maven.plugin.MojoFailureException;

/**
 * Exception thrown when the engine configuration fails.
 */
public class EngineConfigureMojoException extends MojoFailureException {
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message
     *            Message for the exception.
     */
    public EngineConfigureMojoException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message
     *            Message for the exception.
     * @param cause
     *            Cause of the exception.
     */
    public EngineConfigureMojoException(String message, Throwable cause) {
        super(message, cause);
    }
}
