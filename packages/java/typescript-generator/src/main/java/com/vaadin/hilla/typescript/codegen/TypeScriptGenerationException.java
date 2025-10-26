package com.vaadin.hilla.typescript.codegen;

/**
 * Exception thrown when TypeScript code generation fails.
 */
public class TypeScriptGenerationException extends RuntimeException {

    /**
     * Creates a new exception with a message.
     *
     * @param message
     *            the error message
     */
    public TypeScriptGenerationException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with a message and cause.
     *
     * @param message
     *            the error message
     * @param cause
     *            the cause
     */
    public TypeScriptGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
