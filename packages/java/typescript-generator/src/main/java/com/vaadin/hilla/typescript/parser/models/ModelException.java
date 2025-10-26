package com.vaadin.hilla.typescript.parser.models;

public class ModelException extends RuntimeException {
    ModelException(String message) {
        super(message);
    }

    ModelException(String message, Throwable cause) {
        super(message, cause);
    }

    ModelException(Throwable cause) {
        super(cause);
    }
}
