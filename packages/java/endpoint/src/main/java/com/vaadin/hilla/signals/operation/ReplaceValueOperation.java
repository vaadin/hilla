package com.vaadin.hilla.signals.operation;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.StateEvent;

public record ReplaceValueOperation<T>(String operationId, T expected, T value) implements ValueOperation<T> {

    public static <T> ReplaceValueOperation<T> of(ObjectNode event, Class<T> valueType) {
        var rawValue = StateEvent.extractValue(event, true);
        var rawExpected = StateEvent.extractExpected(event, true);
        return new ReplaceValueOperation<>(StateEvent.extractId(event),
            StateEvent.convertValue(rawExpected, valueType),
            StateEvent.convertValue(rawValue, valueType));
    }

    public static <T> ReplaceValueOperation<T> of(String operationId, T expected, T value) {
        return new ReplaceValueOperation<>(operationId, expected, value);
    }
}
