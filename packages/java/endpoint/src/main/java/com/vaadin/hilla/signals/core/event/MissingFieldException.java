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
 * An exception thrown when a required field is missing in the JSON
 * representation of a state event.
 */
public class MissingFieldException extends RuntimeException {
    public MissingFieldException(String fieldName) {
        super("Missing field: " + fieldName);
    }
}
