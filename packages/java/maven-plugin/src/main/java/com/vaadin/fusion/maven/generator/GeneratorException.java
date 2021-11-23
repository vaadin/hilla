package com.vaadin.fusion.maven.generator;

public class GeneratorException extends RuntimeException {
    GeneratorException(String message) {
        super(message);
    }

    GeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    GeneratorException(Throwable cause) {
        super(cause);
    }
}
