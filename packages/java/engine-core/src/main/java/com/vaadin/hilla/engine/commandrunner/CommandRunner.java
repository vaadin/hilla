/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.engine.commandrunner;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.vaadin.flow.server.frontend.FrontendUtils;

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
     * <p>
     * </p>
     * Output and errors destination for the sub-process are the same as the
     * parent process.
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
        run(stdIn, true);
    }

    /**
     * Run the command.
     *
     * @param stdIn
     *            a Consumer that can be used to write to the command's standard
     *            input, can be {@code null} if there's no need to write to it.
     * @param stdOut
     *            whether output and errors destination for the sub-process be
     *            the same as the parent process or not
     *
     * @throws CommandRunnerException
     *             if the command fails
     */
    default void run(Consumer<OutputStream> stdIn, boolean stdOut)
            throws CommandRunnerException {
        if (stdOut) {
            run(stdIn, (stdOutStream) -> {
                new BufferedReader(new InputStreamReader(stdOutStream)).lines()
                        .forEach(System.out::println);
            }, (stdErrStream) -> {
                new BufferedReader(new InputStreamReader(stdErrStream)).lines()
                        .forEach(System.err::println);
            });
        } else {
            run(stdIn, null, null);
        }
    }

    /**
     * Run the command.
     * <p>
     * </p>
     * Output and errors destination for the sub-process are the same as the
     * parent process.
     *
     * @param stdIn
     *            a Consumer that can be used to write to the command's standard
     *            input, can be {@code null} if there's no need to write to it.
     * @param stdOut
     *            a Consumer that can be used to read the command's standard
     *            output, can be {@code null} if there's no need to read it.
     * @param stdErr
     *            a Consumer that can be used to read the command's error
     *            output, can be {@code null} if there's no need to read it.
     *
     * @throws CommandRunnerException
     *             if the command fails
     */
    default void run(Consumer<OutputStream> stdIn, Consumer<InputStream> stdOut,
            Consumer<InputStream> stdErr) throws CommandRunnerException {
        var execs = executables();
        // Find the first executable that works
        var executable = execs.stream().filter(this::executeWithTestArguments)
                .findFirst().orElseThrow(() -> new CommandNotFoundException(
                        "No valid executable found between " + execs));
        getLogger().debug("Running command {}", executable);
        // Execute the command with the given arguments
        executeCommand(executable, arguments(), stdIn, stdOut, stdErr);
    }

    private boolean executeWithTestArguments(String command) {
        try {
            var args = testArguments();
            getLogger().debug("Testing command {} with arguments {}", command,
                    args);
            // Execute the command with the test arguments
            executeCommand(command, args, null, null, null);

            return true;
        } catch (CommandRunnerException e) {
            getLogger().debug("Testing command {} failed", command, e);

            return false;
        }
    }

    private void executeCommand(String executable, String[] arguments,
            Consumer<OutputStream> stdIn, Consumer<InputStream> stdOut,
            Consumer<InputStream> stdErr) throws CommandRunnerException {
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
            var processBuilder = createProcessBuilder(commandWithArgs);

            var process = processBuilder.start();

            var threads = Stream
                    .of(new Pipe<>(stdOut, process.getInputStream()),
                            new Pipe<>(stdErr, process.getErrorStream()),
                            new Pipe<>(stdIn, process.getOutputStream()))
                    .filter(handler -> handler.consumer() != null)
                    .map(handler -> {
                        var t = new Thread(() -> {
                            try (var stream = handler.stream()) {
                                ((Consumer<Closeable>) handler.consumer())
                                        .accept(stream);
                            } catch (IOException e) {
                                getLogger().error("Error while handling stream",
                                        e);
                            }
                        });
                        t.start();
                        return t;
                    }).toList();

            exitCode = process.waitFor();

            for (var thread : threads) {
                thread.join();
            }
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
     *
     * @see CommandRunner#environment()
     *
     * @return a ProcessBuilder instance to be used for executing the passed in
     *         commands and arguments.
     */
    default ProcessBuilder createProcessBuilder(List<String> commandWithArgs) {
        var builder = new ProcessBuilder(commandWithArgs)
                .directory(currentDirectory());

        builder.environment().putAll(environment());

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
    // end of interface (comment added to help formatter)
}

// used internally to pair consumers with their streams
record Pipe<T extends Closeable>(Consumer<T> consumer, T stream) {
}
