package dev.hilla.internal.runner;

public class CommandNotFoundException extends RunnerException {

    public CommandNotFoundException(String message) {
        super(message);
    }

    public CommandNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
