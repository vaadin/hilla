package com.vaadin.hilla.maven;

import org.apache.maven.plugin.MojoFailureException;

public class EngineGenerateMojoException extends MojoFailureException {
    public EngineGenerateMojoException(String message) {
        super(message);
    }

    public EngineGenerateMojoException(String message, Throwable cause) {
        super(message, cause);
    }
}
