package com.vaadin.hilla.engine.commandrunner;

/**
 * Thrown by {@link CommandRunner} when a command is not available.
 */
public class CommandNotFoundException extends CommandRunnerException {

    public CommandNotFoundException(String message) {
        super(message);
    }

    public CommandNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
