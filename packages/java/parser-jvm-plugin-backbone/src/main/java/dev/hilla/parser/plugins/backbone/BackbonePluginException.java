package dev.hilla.parser.plugins.backbone;

import dev.hilla.parser.models.Model;

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
