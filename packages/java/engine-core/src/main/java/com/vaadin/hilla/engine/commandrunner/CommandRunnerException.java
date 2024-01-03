package com.vaadin.hilla.engine.commandrunner;

/**
 * A generic exception thrown by a {@link CommandRunner}.
 */
public class CommandRunnerException extends Exception {

    public CommandRunnerException(String message) {
        super(message);
    }

    public CommandRunnerException(String message, Throwable cause) {
        super(message, cause);
    }
}
