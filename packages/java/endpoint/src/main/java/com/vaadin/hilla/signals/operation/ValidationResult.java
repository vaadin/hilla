/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */

package com.vaadin.hilla.signals.operation;

public class ValidationResult {

    public enum Status {
        ALLOWED, REJECTED
    }

    private final Status status;
    private final String errorMessage;

    private ValidationResult(Status status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    private ValidationResult(Status status) {
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
        return status == Status.ALLOWED;
    }

    public boolean isRejected() {
        return status == Status.REJECTED;
    }

    public static ValidationResult reject(String errorMessage) {
        return new ValidationResult(Status.REJECTED, errorMessage);
    }

    public static ValidationResult allow() {
        return new ValidationResult(Status.ALLOWED);
    }
}
