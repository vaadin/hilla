package com.vaadin.hilla.signals.operation;

@FunctionalInterface
public interface OperationValidator {
    ValidationResult validate(SignalOperation operation);
}
