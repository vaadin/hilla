package com.vaadin.hilla.signals.operation;

public record ReplaceValueOperation<T>(String operationId, T expected, T value) {}
