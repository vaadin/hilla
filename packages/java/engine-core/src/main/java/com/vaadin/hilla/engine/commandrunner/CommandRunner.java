package com.vaadin.hilla.engine.commandrunner;

import com.vaadin.flow.server.frontend.FrontendUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
        var execs = executables();
        // Find the first executable that works
        var executable = execs.stream().filter(this::executeWithTestArguments)
                .findFirst().orElseThrow(() -> new CommandNotFoundException(
                        "No valid executable found between " + execs));
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
            var processBuilder = createProcessBuilder(commandWithArgs, stdOut);

            var process = processBuilder.start();

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
     * Constructs a ProcessBuilder Instance using the passed in commands and
     * arguments.
     * <p>
     * NOTE: This method uses the result of calling
     * {@link CommandRunner#environment()} to set the environment variables of
     * the process to be constructed.
     *
     * @param commandWithArgs
     *            the command to be executed and its arguments
     * @param stdOut
     *            whether output and errors destination for the sub-process be
     *            the same as the parent process or not
     *
     * @see CommandRunner#environment()
     *
     * @return a ProcessBuilder instance to be used for executing the passed in
     *         commands and arguments.
     */
    default ProcessBuilder createProcessBuilder(List<String> commandWithArgs,
            boolean stdOut) {
        var builder = new ProcessBuilder(commandWithArgs)
                .directory(currentDirectory());

        builder.environment().putAll(environment());

        if (stdOut) {
            builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT);
        }

        return builder;
    }

    /**
     * Fetches the Java executable path that initiated the current java process
     *
     * @return An Optional of type String containing Java executable path or an
     *         empty Optional if it was not available.
     */
    default ProcessHandle.Info getCurrentProcessInfo() {
        return ProcessHandle.current().info();
    }

    /**
     * First tries to extract JAVA_HOME from the path of java executable that is
     * used to run the current running java process. If that is not available,
     * then it looks for the {@code System.getProperty("java.home")} that always
     * has a value, but returning the Java path from project's configurations
     * relies on IDE's functionality for setting the "java.home" system property
     * based on the settings of the project.
     *
     * @return A String "path/to/java/home" of current running application or
     *         the path returned by {@code System.getProperty("java.home")}
     */
    private String getCurrentJavaProcessJavaHome() {
        return getCurrentProcessInfo().command().map(javaExecPath -> {
            String pathToExclude = IS_WINDOWS ? "\\bin\\java" : "/bin/java";
            return javaExecPath.substring(0,
                    javaExecPath.lastIndexOf(pathToExclude));
        }).orElse(System.getProperty("java.home"));
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
        return Map.of("JAVA_HOME", getCurrentJavaProcessJavaHome());
    }
}
