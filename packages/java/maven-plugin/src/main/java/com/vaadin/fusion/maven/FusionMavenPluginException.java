package com.vaadin.fusion.maven;

public class FusionMavenPluginException  extends RuntimeException {
    FusionMavenPluginException(String message) {
        super(message);
    }

    FusionMavenPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    FusionMavenPluginException(Throwable cause) {
        super(cause);
    }
}
