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

    /**
     * Get the arguments to use when testing if the command is available.
     *
     * @return the arguments to use when testing if the command is available
     */
    String[] testArguments();

    /**
     * Get the arguments to use when running the command.
     *
     * @return the arguments to use when running the command
     */
    String[] arguments();

    /**
     * Get the logger to use.
     *
     * @return the logger to use
     */
    Logger getLogger();

    /**
     * Get the directory to run the command in.
     *
     * @return the directory to run the command in
     */
    File currentDirectory();

    /**
     * Get the executables to try, in order of preference.
     *
     * @return the executables to try
     */
    List<String> executables();

    /**
     * Run the command.
     * 
     * @param stdIn
     *            a Consumer that can be used to write to the command's standard
     *            input, can be {@code null} if there's no need to write to it.
     *
     * @throws CommandRunnerException
     *             if the command fails
     */
    default void run(Consumer<OutputStream> stdIn)
            throws CommandRunnerException {
        // Find the first executable that works
        var executable = executables().stream()
                .filter(this::executeWithTestArguments).findFirst()
                .orElseThrow(() -> new CommandNotFoundException(
                        "No valid executable found"));
        getLogger().debug("Running command {}", executable);
        // Execute the command with the given arguments
        executeCommand(executable, arguments(), stdIn);
    }

    private boolean executeWithTestArguments(String command) {
        try {
            var args = testArguments();
            getLogger().debug("Testing command {} with arguments {}", command,
                    args);
            // Execute the command with the test arguments
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
                // Allow the caller to write to the command's standard input
                try (var outputStream = process.getOutputStream()) {
                    stdIn.accept(outputStream);
                }
            }

            exitCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
            // Tries to figure out if the command is not found. This is not a
            // 100% reliable way to do it, but an exception will be thrown
            // anyway.
            if (e.getCause() != null && e.getCause().getMessage() != null
                    && e.getCause().getMessage()
                            .contains("No such file or directory")) {
                throw new CommandNotFoundException(
                        "Command or file not found: " + executable, e);
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
