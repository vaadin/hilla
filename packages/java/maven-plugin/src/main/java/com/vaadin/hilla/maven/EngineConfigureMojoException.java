package com.vaadin.hilla.maven;

import org.apache.maven.plugin.MojoFailureException;

public class EngineConfigureMojoException extends MojoFailureException {
    public EngineConfigureMojoException(String message) {
        super(message);
    }

    public EngineConfigureMojoException(String message, Throwable cause) {
        super(message, cause);
    }
}
