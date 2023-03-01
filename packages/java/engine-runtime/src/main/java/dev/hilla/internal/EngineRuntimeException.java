package dev.hilla.internal;

public class EngineRuntimeException extends RuntimeException {
    EngineRuntimeException(String message) {
        super(message);
    }

    EngineRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    EngineRuntimeException(Throwable cause) {
        super(cause);
    }
}
