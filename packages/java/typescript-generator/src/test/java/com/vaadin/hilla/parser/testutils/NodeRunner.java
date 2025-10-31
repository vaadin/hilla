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
package com.vaadin.hilla.parser.testutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Utility for running Node.js scripts from Java tests.
 */
public class NodeRunner {
    private final File workingDirectory;
    private String nodeCommand = "node";
    private int timeoutSeconds = 60;

    public NodeRunner(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public NodeRunner nodeCommand(String nodeCommand) {
        this.nodeCommand = nodeCommand;
        return this;
    }

    public NodeRunner timeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    /**
     * Execute a Node.js script with the given arguments and input.
     *
     * @param scriptPath
     *            Path to the Node.js script to execute
     * @param args
     *            Additional arguments to pass to the script
     * @param input
     *            Optional input to pass via stdin
     * @return The output from the script
     * @throws NodeExecutionException
     *             if the script fails or times out
     */
    public NodeResult execute(String scriptPath, List<String> args,
            String input) throws NodeExecutionException {
        List<String> command = new ArrayList<>();
        command.add(nodeCommand);
        command.add(scriptPath);
        command.addAll(args);

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workingDirectory);
            pb.redirectErrorStream(false);

            Process process = pb.start();

            // Write input if provided
            if (input != null && !input.isEmpty()) {
                process.getOutputStream()
                        .write(input.getBytes(StandardCharsets.UTF_8));
                process.getOutputStream().close();
            }

            // Read stdout
            StringBuilder stdout = new StringBuilder();
            BufferedReader stdoutReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(),
                            StandardCharsets.UTF_8));
            String line;
            while ((line = stdoutReader.readLine()) != null) {
                stdout.append(line).append("\n");
            }

            // Read stderr
            StringBuilder stderr = new StringBuilder();
            BufferedReader stderrReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(),
                            StandardCharsets.UTF_8));
            while ((line = stderrReader.readLine()) != null) {
                stderr.append(line).append("\n");
            }

            boolean finished = process.waitFor(timeoutSeconds,
                    TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new NodeExecutionException(
                        "Node.js process timed out after " + timeoutSeconds
                                + " seconds");
            }

            int exitCode = process.exitValue();
            String stdoutStr = stdout.toString();
            String stderrStr = stderr.toString();

            if (exitCode != 0) {
                throw new NodeExecutionException(
                        "Node.js process failed with exit code " + exitCode
                                + "\nStderr: " + stderrStr + "\nStdout: "
                                + stdoutStr);
            }

            return new NodeResult(stdoutStr, stderrStr, exitCode);

        } catch (IOException e) {
            throw new NodeExecutionException(
                    "Failed to execute Node.js script: " + scriptPath, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NodeExecutionException(
                    "Node.js execution was interrupted", e);
        }
    }

    /**
     * Execute a Node.js script with arguments.
     */
    public NodeResult execute(String scriptPath, List<String> args)
            throws NodeExecutionException {
        return execute(scriptPath, args, null);
    }

    /**
     * Execute a Node.js script with input via stdin.
     */
    public NodeResult execute(String scriptPath, String input)
            throws NodeExecutionException {
        return execute(scriptPath, List.of(), input);
    }

    /**
     * Execute a Node.js script without arguments or input.
     */
    public NodeResult execute(String scriptPath) throws NodeExecutionException {
        return execute(scriptPath, List.of(), null);
    }

    /**
     * Result of a Node.js script execution.
     */
    public static class NodeResult {
        private final String stdout;
        private final String stderr;
        private final int exitCode;

        public NodeResult(String stdout, String stderr, int exitCode) {
            this.stdout = stdout;
            this.stderr = stderr;
            this.exitCode = exitCode;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }

        public int getExitCode() {
            return exitCode;
        }
    }

    /**
     * Exception thrown when Node.js execution fails.
     */
    public static class NodeExecutionException extends Exception {
        public NodeExecutionException(String message) {
            super(message);
        }

        public NodeExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
