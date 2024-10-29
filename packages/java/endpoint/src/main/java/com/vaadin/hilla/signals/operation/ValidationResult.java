package com.vaadin.hilla.signals.operation;

public class ValidationResult {

    public enum Status {
        OK, REJECTED
    }

    private final Status status;
    private final String errorMessage;

    public ValidationResult(Status status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public ValidationResult(Status status) {
        this.status = status;
        this.errorMessage = null;
    }

    public Status getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isOk() {
        return status == Status.OK;
    }

    public boolean isRejected() {
        return status == Status.REJECTED;
    }

    public static ValidationResult rejected(String errorMessage) {
        return new ValidationResult(Status.REJECTED, errorMessage);
    }

    public static ValidationResult ok() {
        return new ValidationResult(Status.OK);
    }
}
