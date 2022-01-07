package dev.hilla.maven;

import org.apache.maven.plugin.MojoFailureException;

public class HillaGeneratorMojoException extends MojoFailureException {
    public HillaGeneratorMojoException(String message) {
        super(message);
    }

    public HillaGeneratorMojoException(String message, Throwable cause) {
        super(message, cause);
    }
}
