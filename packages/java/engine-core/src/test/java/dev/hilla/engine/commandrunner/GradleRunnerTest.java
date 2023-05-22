package dev.hilla.engine.commandrunner;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class GradleRunnerTest {

    @Test
    void shouldNotCreateRunnerForUnknownProjectType() throws IOException {
        Path tmpDir = null;

        try {
            tmpDir = Files.createTempDirectory("prepareCommandGradle");
            var runner = GradleRunner.forProject(tmpDir.toFile());
            assertTrue(runner.isEmpty());
        } finally {
            if (tmpDir != null) {
                Files.deleteIfExists(tmpDir);
            }
        }
    }
}
