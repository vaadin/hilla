package dev.hilla.engine.commandrunner;

import com.vaadin.flow.server.frontend.FrontendUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        executeCommand(executable, arguments(), stdIn, true);
    }

    private boolean executeWithTestArguments(String command) {
        try {
            var args = testArguments();
            getLogger().debug("Testing command {} with arguments {}", command,
                    args);
            // Execute the command with the test arguments
            executeCommand(command, args, null, false);

            return true;
        } catch (CommandRunnerException e) {
            getLogger().debug("Testing command {} failed", command, e);

            return false;
        }
    }

    private void executeCommand(String executable, String[] arguments,
            Consumer<OutputStream> stdIn, boolean stdOut)
            throws CommandRunnerException {
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
                    .directory(currentDirectory());

            builder.environment().putAll(environment());

            if (stdOut) {
                builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .redirectError(ProcessBuilder.Redirect.INHERIT);
            }

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

    /**
     * First tries to extract JAVA_HOME from the path of java executable that is
     * used to run the current running java process. If that is not available,
     * then it looks for the {@code System.getProperty("java.home")} that relies
     * on IDE's functionality for setting the "java.home" system property based
     * on the settings of the project. If any of the above where available, an
     * Optional of type String containing "path/to/current/running/java/home" is
     * returned, otherwise, an empty Optional will be returned.
     *
     * @return Optional of type String containing "path/to/java/home" of current
     *         running application or the path returned by
     *         System.getProperty("java.home") if available, otherwise, an empty
     *         Optional will be returned.
     */
    private Optional<String> getCurrentJavaProcessJavaHome() {
        return ProcessHandle.current().info().command()
                .map(javaExecPath -> javaExecPath.substring(0,
                        javaExecPath.lastIndexOf("/bin/java")))
                .or(() -> System.getProperty("java.home").describeConstable());
    }

    /**
     * The custom environment variables needed for running the commands can be
     * provided using this method.
     * <p>
     * NOTE: The provided environment variables by this method is added to the
     * existing env of the ProcessBuilder, and in case of providing a duplicate
     * key, it is overwriting the previously existing value.
     *
     * @return A java.util.Map containing the environment variables and their
     *         values that are used by the ProcessBuilder to execute the
     *         command.
     */
    default Map<String, String> environment() {
        return getCurrentJavaProcessJavaHome()
                .map(javaHome -> Map.of("JAVA_HOME", javaHome))
                .orElse(Map.of());
    }
}
