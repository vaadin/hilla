package dev.hilla.internal;

public final class ParserException extends RuntimeException {
    ParserException(String message) {
        super(message);
    }

    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }

    ParserException(Throwable cause) {
        super(cause);
    }
}
