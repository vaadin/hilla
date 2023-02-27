package dev.hilla.internal.runner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class GradleRunner extends CommandRunner {

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
