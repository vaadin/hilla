package com.vaadin.hilla.signals.operation;

public record SetValueOperation<T>(String operationId, T value) {}
