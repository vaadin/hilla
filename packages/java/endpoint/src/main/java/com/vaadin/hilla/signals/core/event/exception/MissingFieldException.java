package com.vaadin.hilla.signals.core.event.exception;

/**
 * An exception thrown when a required field is missing in the JSON
 * representation of a state event.
 */
public class MissingFieldException extends RuntimeException {
    public MissingFieldException(String fieldName) {
        super("Missing field: " + fieldName);
    }
}
