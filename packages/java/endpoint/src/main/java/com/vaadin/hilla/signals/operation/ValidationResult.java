/*
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
