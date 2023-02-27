package dev.hilla.internal.runner;

/**
 * A generic exception thrown by a {@link CommandRunner}.
 */
public class RunnerException extends Exception {

    public RunnerException(String message) {
        super(message);
    }

    public RunnerException(String message, Throwable cause) {
        super(message, cause);
    }

    public RunnerException(Throwable cause) {
        super(cause);
    }
}
