package dev.hilla.parser.core;

public class WalkerException extends RuntimeException {
    WalkerException(String message) {
        super(message);
    }

    WalkerException(String message, Throwable cause) {
        super(message, cause);
    }

    WalkerException(Throwable cause) {
        super(cause);
    }
}
