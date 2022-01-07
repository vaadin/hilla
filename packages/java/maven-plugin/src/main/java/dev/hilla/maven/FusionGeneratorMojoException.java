package dev.hilla.maven;

import org.apache.maven.plugin.MojoFailureException;

public class FusionGeneratorMojoException extends MojoFailureException {
    public FusionGeneratorMojoException(String message) {
        super(message);
    }

    public FusionGeneratorMojoException(String message, Throwable cause) {
        super(message, cause);
    }
}
