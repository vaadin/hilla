/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */

package com.vaadin.hilla.signals.core.event;

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
