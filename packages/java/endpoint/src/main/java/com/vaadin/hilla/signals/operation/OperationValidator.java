package com.vaadin.hilla.signals.operation;

@FunctionalInterface
public interface OperationValidator<T> {
    ValidationResult validate(SignalOperation<T> operation);
}
