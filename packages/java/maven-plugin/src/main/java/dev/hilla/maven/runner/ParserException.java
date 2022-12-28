package dev.hilla.maven.runner;

public final class ParserException extends RuntimeException {
    ParserException(String message) {
        super(message);
    }

    ParserException(String message, Throwable cause) {
        super(message, cause);
    }

    ParserException(Throwable cause) {
        super(cause);
    }
}
