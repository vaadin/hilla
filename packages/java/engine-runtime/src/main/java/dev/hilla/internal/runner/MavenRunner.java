package dev.hilla.internal.runner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.frontend.FrontendUtils;

/**
 * Runs a Maven command.
 */
public class MavenRunner implements CommandRunner {
    private static final boolean IS_WINDOWS = FrontendUtils.isWindows();
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MavenRunner.class);
    private final String[] args;
    private final Path projectDir;
    private final boolean windowsOS;

    /**
     * Creates a Maven runner.
     *
     * @param projectDir
     *            the project directory
     * @param windowsOS
     *            whether the OS is Windows
     * @param args
     *            the arguments to pass to the Maven command
     */
    public MavenRunner(Path projectDir, boolean windowsOS, String... args) {
        this.projectDir = projectDir;
        this.windowsOS = windowsOS;
        this.args = args;
    }

    /**
     * Creates a Maven runner for the given project directory.
     *
     * @param projectDir
     *            the project directory
     * @param args
     *            the arguments to pass to the Maven command
     * @return a Maven runner if the project directory contains a Maven project,
     *         an empty optional otherwise
     */
    public static Optional<CommandRunner> forProject(Path projectDir,
            String... args) {
        if (Files.exists(projectDir.resolve("pom.xml"))) {
            return Optional.of(new MavenRunner(projectDir, IS_WINDOWS, args));
        }

        return Optional.empty();
    }

    /**
     * Selects the maven executable to use. Prefer the maven wrapper if it
     * exists.
     */
    public String chooseExecutable() {
        var command = wrapperExecutable();

        if (Files.exists(projectDir.resolve(command))) {
            return command;
        } else {
            LOGGER.debug("No maven wrapper found");
            return mavenExecutable();
        }
    }

    /**
     * Returns the name of the maven executable, depending on the OS.
     */
    public String mavenExecutable() {
        return windowsOS ? "mvn.cmd" : "mvn";
    }

    @Override
    public void run() throws RunnerException {
        var command = new ArrayList<String>();
        command.add(chooseExecutable());
        command.addAll(Arrays.asList(args));

        var exitCode = 0;
        try {
            LOGGER.debug("Running command: {}", command);
            var builder = new ProcessBuilder(command)
                    .directory(projectDir.toFile()).inheritIO();
            exitCode = builder.start().waitFor();
        } catch (IOException e) {
            if (e.getCause() != null && e.getCause().getMessage()
                    .contains("No such file or directory")) {
                LOGGER.error(
                        """
                                No maven executable found. You can install the Maven Wrapper by
                                running the following command in your project directory:
                                `mvn wrapper:wrapper` where `mvn` is the path to your Maven executable.
                                Alternatively, you can add the Maven executable to your PATH environment variable.
                                """);
            }
            throw new RunnerException("Maven not found", e);
        } catch (Exception e) {
            throw new RunnerException("Maven command execution failed", e);
        }
        if (exitCode != 0) {
            throw new RunnerException(
                    "Maven command execution failed with exit code" + exitCode);
        }
    }

    /**
     * Returns the name of the maven wrapper executable, depending on the OS.
     */
    public String wrapperExecutable() {
        return windowsOS ? ".\\mvnw.cmd" : "./mvnw";
    }
}
