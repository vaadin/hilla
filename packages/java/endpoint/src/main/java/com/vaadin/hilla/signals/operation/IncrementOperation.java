package com.vaadin.hilla.signals.operation;

public record IncrementOperation(String operationId, Double value) implements ValueOperation<Double> {

    public static IncrementOperation of(String operationId, Double value) {
        return new IncrementOperation(operationId, value);
    }
}
