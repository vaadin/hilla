package dev.hilla.engine.commandrunner;

import com.vaadin.flow.server.frontend.FrontendUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A generic command runner which throws a {@link CommandRunnerException}.
 */
public interface CommandRunner {
    static final boolean IS_WINDOWS = FrontendUtils.isWindows();

    String[] testArguments();

    String[] arguments();

    Logger getLogger();

    File currentDirectory();

    List<String> executables();

    /**
     * Run the command.
     *
     * @throws CommandRunnerException
     *             if the command fails
     */
    default void run() throws CommandRunnerException {
        var executable = executables().stream()
                .filter(this::runWithTestArguments).findFirst()
                .orElseThrow(() -> new CommandNotFoundException(
                        "No valid executable found"));
        executeCommand(executable, arguments());
    }

    private boolean runWithTestArguments(String command) {
        try {
            var args = testArguments();

            if (getLogger().isTraceEnabled()) {
                getLogger().trace("Testing command {} with arguments {}",
                        command, args);
            }

            executeCommand(command, args);

            return true;
        } catch (CommandRunnerException e) {
            getLogger().debug("Testing command {} failed", command, e);

            return false;
        }
    }

    private void executeCommand(String executable, String[] arguments)
            throws CommandRunnerException {
        var commandWithArgs = Stream
                .concat(Stream.of(executable), Arrays.stream(arguments))
                .collect(Collectors.toList());

        if (IS_WINDOWS)
            commandWithArgs = List.of("cmd.exe", "/c", commandWithArgs.stream()
                    .map(arg -> (arg.contains(" ") ? "\"" + arg + "\"" : arg))
                    .collect(Collectors.joining(" ", "\"", "\"")));

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Running command: {} in directory {}",
                    commandWithArgs, currentDirectory());
        }

        var exitCode = 0;

        try {
            var builder = new ProcessBuilder(commandWithArgs)
                    .directory(currentDirectory()).inheritIO();
            exitCode = builder.start().waitFor();
        } catch (IOException | InterruptedException e) {
            if (e.getCause() != null && e.getCause().getMessage()
                    .contains("No such file or directory")) {
                throw new CommandNotFoundException(
                        "Command not found: " + executable, e);
            }

            throw new CommandRunnerException(
                    "Failed to execute command: " + executable, e);
        }
        if (exitCode != 0) {
            throw new CommandRunnerException("Command failed with exit code "
                    + exitCode + ": " + executable);
        }
    }
}
