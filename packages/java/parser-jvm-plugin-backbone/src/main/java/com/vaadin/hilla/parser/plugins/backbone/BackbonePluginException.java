package com.vaadin.hilla.parser.plugins.backbone;

public class BackbonePluginException extends RuntimeException {
    BackbonePluginException(String message) {
        super(message);
    }

    BackbonePluginException(String message, Throwable cause) {
        super(message, cause);
    }

    BackbonePluginException(Throwable cause) {
        super(cause);
    }
}
