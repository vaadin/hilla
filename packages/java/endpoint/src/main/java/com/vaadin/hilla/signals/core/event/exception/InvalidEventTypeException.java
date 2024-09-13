package com.vaadin.hilla.signals.core.event.exception;

/**
 * An exception thrown when the event type is null or invalid.
 */
public class InvalidEventTypeException extends RuntimeException {
    public InvalidEventTypeException(String message) {
        super(message);
    }

    public InvalidEventTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
