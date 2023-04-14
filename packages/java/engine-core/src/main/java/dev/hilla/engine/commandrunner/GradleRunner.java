package dev.hilla.engine.commandrunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Runs a Gradle command.
 */
public class GradleRunner implements CommandRunner {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(GradleRunner.class);

    private final File projectDir;

    /**
     * Creates a Gradle runner.
     *
     * @param projectDir
     *            the project directory
     */
    public GradleRunner(File projectDir) {
        this.projectDir = projectDir;
    }

    /**
     * Creates a Gradle runner for the given project directory.
     *
     * @param projectDir
     *            the project directory
     * @return a Gradle runner if the project directory contains a Gradle
     *         project, an empty optional otherwise
     */
    public static Optional<CommandRunner> forProject(File projectDir) {
        if (new File(projectDir, "build.gradle").exists()) {
            return Optional.of(new GradleRunner(projectDir));
        }

        return Optional.empty();
    }

    @Override
    public String[] arguments() {
        throw new UnsupportedOperationException("Gradle is not supported yet");
    }

    @Override
    public String[] testArguments() {
        throw new UnsupportedOperationException("Gradle is not supported yet");
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public File currentDirectory() {
        return projectDir;
    }

    @Override
    public List<String> executables() {
        throw new UnsupportedOperationException("Gradle is not supported yet");
    }

    @Override
    public void run(Consumer<OutputStream> stdIn)
            throws CommandRunnerException {
        throw new UnsupportedOperationException("Gradle is not supported yet");
    }
}
