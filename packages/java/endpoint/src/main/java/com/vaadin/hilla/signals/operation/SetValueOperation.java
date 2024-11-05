package com.vaadin.hilla.signals.operation;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.signals.core.event.StateEvent;

public record SetValueOperation<T>(String operationId, T value) implements ValueOperation<T> {

    public static <T> SetValueOperation<T> of(ObjectNode event, Class<T> valueType) {
        var rawValue = StateEvent.extractValue(event, true);
        return new SetValueOperation<>(StateEvent.extractId(event),
            StateEvent.convertValue(rawValue, valueType));
    }
}
