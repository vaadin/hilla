package com.vaadin.fusion.maven;

public class GeneratorMavenPluginException extends RuntimeException {
    GeneratorMavenPluginException(String message) {
        super(message);
    }

    GeneratorMavenPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    GeneratorMavenPluginException(Throwable cause) {
        super(cause);
    }
}
