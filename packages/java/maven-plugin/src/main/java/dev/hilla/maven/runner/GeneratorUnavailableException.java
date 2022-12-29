package dev.hilla.maven.runner;

public class GeneratorUnavailableException extends Exception {
    public GeneratorUnavailableException(String message) {
        super(message);
    }

    public GeneratorUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
