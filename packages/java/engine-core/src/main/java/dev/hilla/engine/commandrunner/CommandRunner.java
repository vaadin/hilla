package dev.hilla.engine.commandrunner;

import com.vaadin.flow.server.frontend.FrontendUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A generic command runner which throws a {@link CommandRunnerException}.
 */
public interface CommandRunner {
    boolean IS_WINDOWS = FrontendUtils.isWindows();

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
    default void run(Consumer<OutputStream> stdIn)
            throws CommandRunnerException {
        var executable = executables().stream()
                .filter(this::runWithTestArguments).findFirst()
                .orElseThrow(() -> new CommandNotFoundException(
                        "No valid executable found"));
        getLogger().debug("Running command {}", executable);
        executeCommand(executable, arguments(), stdIn);
    }

    private boolean runWithTestArguments(String command) {
        try {
            var args = testArguments();
            getLogger().debug("Testing command {} with arguments {}", command,
                    args);
            executeCommand(command, args, null);

            return true;
        } catch (CommandRunnerException e) {
            getLogger().debug("Testing command {} failed", command, e);

            return false;
        }
    }

    private void executeCommand(String executable, String[] arguments,
            Consumer<OutputStream> stdIn) throws CommandRunnerException {
        Stream<String> cmdStream = Stream.concat(Stream.of(executable),
                Arrays.stream(arguments));

        // On Windows, commands are run by cmd.exe and the command line is sent
        // as a single parameter (note that having quotes inside other quotes is
        // fine). On UNIX-like systems, command is run as is.
        var commandWithArgs = IS_WINDOWS
                ? List.of("cmd.exe", "/c", cmdStream.map(
                        arg -> (arg.contains(" ") ? "\"" + arg + "\"" : arg))
                        .collect(Collectors.joining(" ", "\"", "\"")))
                : cmdStream.collect(Collectors.toList());

        if (getLogger().isTraceEnabled()) {
            getLogger().trace("Running command: \"{}\" in directory \"{}\"",
                    String.join(" ", commandWithArgs), currentDirectory());
        }

        var exitCode = 0;

        try {
            var builder = new ProcessBuilder(commandWithArgs)
                    .directory(currentDirectory())
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT);
            var process = builder.start();

            if (stdIn != null) {
                try (var outputStream = process.getOutputStream()) {
                    stdIn.accept(outputStream);
                }
            }

            exitCode = process.waitFor();
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
