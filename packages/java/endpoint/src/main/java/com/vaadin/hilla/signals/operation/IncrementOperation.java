/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */

package com.vaadin.hilla.signals.operation;

public record IncrementOperation(String operationId, Double value) implements ValueOperation<Double> {

    public static IncrementOperation of(String operationId, Double value) {
        return new IncrementOperation(operationId, value);
    }
}
