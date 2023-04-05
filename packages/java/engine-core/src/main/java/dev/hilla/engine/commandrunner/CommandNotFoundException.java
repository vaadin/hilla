package dev.hilla.engine.commandrunner;

public class CommandNotFoundException extends CommandRunnerException {

    public CommandNotFoundException(String message) {
        super(message);
    }

    public CommandNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
