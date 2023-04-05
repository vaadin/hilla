package dev.hilla.internal.runner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.vaadin.flow.server.frontend.FrontendUtils;

/**
 * A generic command runner which throws a {@link RunnerException}.
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
     * @throws RunnerException
     *             if the command fails
     */
    default void run() throws CommandNotFoundException, RunnerException {
        var executable = executables().stream()
                .filter(this::runWithTestArguments).findFirst()
                .orElseThrow(() -> new CommandNotFoundException(
                        "No valid executable found"));
        executeCommand(executable, arguments());
    }

    private boolean runWithTestArguments(String command) {
        try {
            executeCommand(command, testArguments());
        } catch (RunnerException e) {
            getLogger().debug("Testing command {} failed", command, e);
            return false;
        }

        return true;
    }

    private void executeCommand(String executable, String[] arguments)
            throws CommandNotFoundException, RunnerException {
        var args = Stream
                .concat(Stream.of(executable), Arrays.stream(arguments))
                .collect(Collectors.toList());

        if (IS_WINDOWS)
            args = List.of("cmd.exe", "/c", args.stream()
                    .map(arg -> (arg.contains(" ") ? "\"" + arg + "\"" : arg))
                    .collect(Collectors.joining(" ", "\"", "\"")));

        var exitCode = 0;

        try {
            var builder = new ProcessBuilder(args).directory(currentDirectory())
                    .inheritIO();
            exitCode = builder.start().waitFor();
        } catch (IOException | InterruptedException e) {
            if (e.getCause() != null && e.getCause().getMessage()
                    .contains("No such file or directory")) {
                throw new CommandNotFoundException(
                        "Command not found: " + executable, e);
            }

            throw new RunnerException(
                    "Failed to execute command: " + executable, e);
        }
        if (exitCode != 0) {
            throw new RunnerException("Command failed with exit code "
                    + exitCode + ": " + executable);
        }
    }
}
