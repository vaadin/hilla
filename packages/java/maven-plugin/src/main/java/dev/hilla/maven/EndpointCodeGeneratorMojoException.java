package dev.hilla.maven;

import org.apache.maven.plugin.MojoFailureException;

public class EndpointCodeGeneratorMojoException extends MojoFailureException {
    public EndpointCodeGeneratorMojoException(String message) {
        super(message);
    }

    public EndpointCodeGeneratorMojoException(String message, Throwable cause) {
        super(message, cause);
    }
}
