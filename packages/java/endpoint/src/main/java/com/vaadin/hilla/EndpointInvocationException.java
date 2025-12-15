/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.hilla;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown by EndpointInvoker when invocation fails.
 */
public abstract class EndpointInvocationException extends Exception {

    /**
     * Exception indicating the endpoint was not found.
     */
    public static class EndpointNotFoundException
            extends EndpointHttpException {

        /**
         * Creates a new instance..
         */
        public EndpointNotFoundException() {
            super(null);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.NOT_FOUND;
        }
    }

    /**
     * Exception indicating access to the endpoint was denied.
     *
     * @deprecated use {@link EndpointUnauthorizedException} instead.
     */
    @Deprecated(forRemoval = true)
    public static class EndpointAccessDeniedException
            extends EndpointInvocationException {

        /**
         * Creates a new instance.
         *
         * @param message
         *            the message to pass to the client
         */
        public EndpointAccessDeniedException(String message) {
            super(message);
        }

    }

    /**
     * Exception indicating a problem with the request data.
     */
    public static class EndpointBadRequestException
            extends EndpointHttpException {

        /**
         * Creates a new instance.
         *
         * @param message
         *            the message to pass to the client
         */
        public EndpointBadRequestException(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.BAD_REQUEST;
        }

    }

    /**
     * Exception indicating an unexpected server error occured during handling
     * of the endpoint invocation.
     */
    public static class EndpointInternalException
            extends EndpointHttpException {

        /**
         * Creates a new instance.
         *
         * @param message
         *            the message to pass to the client
         */
        public EndpointInternalException(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

    }

    /**
     * Exception indicating access to the endpoint was denied as the user is not
     * authenticated.
     */
    public static class EndpointUnauthorizedException
            extends EndpointHttpException {
        public EndpointUnauthorizedException(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.UNAUTHORIZED;
        }
    }

    /**
     * Exception indicating access to the endpoint was denied as the user is not
     * authorized.
     */
    public static class EndpointForbiddenException
            extends EndpointHttpException {
        public EndpointForbiddenException(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.FORBIDDEN;
        }
    }

    /**
     * Allows to specify the HTTP status code and message to return as error.
     * While most common specialized exceptions are already provided, other can
     * be created by extending this class.
     */
    public static abstract class EndpointHttpException
            extends EndpointInvocationException {
        public EndpointHttpException(String message) {
            super(message);
        }

        /**
         * Returns the HTTP status. Only 4xx and 5xx statuses are allowed.
         *
         * @return the HTTP status
         */
        protected abstract HttpStatus getHttpStatus();

        /**
         * Returns the HTTP status code.
         *
         * @return the HTTP status code
         */
        public final int getHttpStatusCode() {
            return switch (getHttpStatus().series()) {
            case CLIENT_ERROR, SERVER_ERROR -> getHttpStatus().value();
            default -> throw new IllegalArgumentException(
                    "Only 4xx and 5xx status codes are allowed");
            };
        }
    }

    /**
     * Creates a new instance..
     *
     * @param errorMessage
     *            an additional message passed to the client side
     */
    public EndpointInvocationException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Returns the additional message that is passed to the client side.
     *
     * @return the error message or {@code null} if no error message was
     *         provided
     */
    @Override
    public String getMessage() {
        return super.getMessage();
    }

}
