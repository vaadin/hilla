package dev.hilla.internal.runner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Runs a Gradle command.
 */
public class GradleRunner implements CommandRunner {

    /**
     * Creates a Gradle runner for the given project directory.
     *
     * @param projectDir
     *            the project directory
     * @return a Gradle runner if the project directory contains a Gradle
     *         project, an empty optional otherwise
     */
    public static Optional<CommandRunner> forProject(Path projectDir) {
        if (Files.exists(projectDir.resolve("build.gradle"))) {
            return Optional.of(new GradleRunner());
        }

        return Optional.empty();
    }

    @Override
    public void run() throws RunnerException {
        throw new UnsupportedOperationException("Gradle is not supported yet");
    }
}
