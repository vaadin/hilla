/*
 * Copyright 2000-2022 Vaadin Ltd.
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

package com.vaadin.hilla.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A validation exception class that is intended to be thrown when any endpoint
 * method receives invalid parameter(s).
 *
 * Behaves similar to the {@link EndpointException} and contains additional
 * information about the validation errors.
 *
 * @see EndpointException
 */
public class EndpointValidationException extends EndpointException {

    /**
     * A validation error data.
     */
    public static class ValidationErrorData {
        private final String parameterName;
        private final String message;

        private final String validatorMessage;

        /**
         * Creates a validation error data object.
         *
         * @param message
         *            validation error message, mandatory (cannot be
         *            {@code null} or blank)
         * @param parameterName
         *            invalid parameter name, optional (can be {@code null} or
         *            blank)
         * @param validatorMessage
         *            validator message, optional (can be {@code null} or blank)
         */
        public ValidationErrorData(String message, String parameterName,
                String validatorMessage) {
            if (message == null || message.isEmpty()) {
                throw new IllegalArgumentException(
                        "Message cannot be null or empty");
            }
            this.parameterName = parameterName;
            this.message = message;
            this.validatorMessage = validatorMessage;
        }

        /**
         * Creates a validation error data object.
         *
         * @param message
         *            validation error message, mandatory (cannot be
         *            {@code null} or blank)
         * @param parameterName
         *            invalid parameter name, optional (can be {@code null} or
         *            blank)
         */
        public ValidationErrorData(String message, String parameterName) {
            this(message, parameterName, null);
        }

        /**
         * Creates a validation error data object.
         *
         * @param message
         *            validation error message, mandatory (cannot be
         *            {@code null} or blank)
         */
        public ValidationErrorData(String message) {
            this(message, null);
        }

        /**
         * Gets the parameter name that caused the validation error.
         *
         * @return the parameter name, may be {@code null}
         */
        public String getParameterName() {
            return parameterName;
        }

        /**
         * Gets the validation error message.
         *
         * @return the validation error message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Gets th validator message.
         *
         * @return the validator message
         */
        public String getValidatorMessage() {
            return validatorMessage;
        }
    }

    private final transient List<ValidationErrorData> validationErrorData;

    /**
     * Creates a validation exception from the error data.
     *
     * @param data
     *            validation error data, mandatory (cannot be {@code null})
     */
    public EndpointValidationException(ValidationErrorData data) {
        this(Collections.singletonList(Objects.requireNonNull(data,
                "At least one 'validation error' is required")));
    }

    /**
     * Creates a validation exception from a error data list.
     *
     * @param validationErrorData
     *            A list of validation error data, must not be {@code null} or
     *            empty.
     */
    public EndpointValidationException(
            List<ValidationErrorData> validationErrorData) {
        this("Validation failed", validationErrorData);
    }

    /**
     * Creates a validation exception from a error data list.
     *
     * @param message
     *            General error message.
     * @param validationErrorData
     *            A list of validation error data, must not be {@code null} or
     *            empty.
     */
    public EndpointValidationException(String message,
            List<ValidationErrorData> validationErrorData) {
        super(message);
        if (validationErrorData == null || validationErrorData.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one 'validation error' is required");
        }
        this.validationErrorData = Collections
                .unmodifiableList(validationErrorData);
    }

    /**
     * Gets the collection of the data on the validation errors.
     *
     * @return the error data
     */
    public List<ValidationErrorData> getValidationErrorData() {
        return validationErrorData;
    }

    @Override
    public Map<String, Object> getSerializationData() {
        Map<String, Object> serializationData = new HashMap<>(
                super.getSerializationData());
        serializationData.put("validationErrorData", validationErrorData);
        return serializationData;
    }
}
