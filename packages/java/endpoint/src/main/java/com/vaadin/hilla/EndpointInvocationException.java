package com.vaadin.hilla;

/**
 * Exception thrown by EndpointInvoker when invocation fails.
 */
public abstract class EndpointInvocationException extends Exception {

    /**
     * Exception indicating the endpoint was not found.
     */
    public static class EndpointNotFoundException
            extends EndpointInvocationException {

        /**
         * Creates a new instance..
         */
        public EndpointNotFoundException() {
            super(null);
        }

    }

    /**
     * Exception indicating access to the endpoint was denied.
     */
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
            extends EndpointInvocationException {

        /**
         * Creates a new instance.
         *
         * @param message
         *            the message to pass to the client
         */
        public EndpointBadRequestException(String message) {
            super(message);
        }

    }

    /**
     * Exception indicating an unexpected server error occured during handling
     * of the endpoint invocation.
     */
    public static class EndpointInternalException
            extends EndpointInvocationException {

        /**
         * Creates a new instance.
         *
         * @param message
         *            the message to pass to the client
         */
        public EndpointInternalException(String message) {
            super(message);
        }

    }

    /**
     * Allows to specify the HTTP status code and message to return as error.
     * Although the HTTP code is arbitrary, this exception still indicates an
     * error condition.
     */
    public static class EndpointHttpException
            extends EndpointInvocationException {
        private final int httpStatusCode;

        /**
         * Creates a new instance.
         *
         * @param httpStatusCode
         *            the HTTP status code to return
         * @param message
         *            the message to pass to the client
         */
        public EndpointHttpException(int httpStatusCode, String message) {
            super(message);
            this.httpStatusCode = httpStatusCode;
        }

        /**
         * Returns the HTTP status code.
         *
         * @return the HTTP status code
         */
        public int getHttpStatusCode() {
            return httpStatusCode;
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
