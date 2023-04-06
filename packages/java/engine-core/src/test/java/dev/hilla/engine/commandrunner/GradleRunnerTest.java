package dev.hilla.engine.commandrunner;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class GradleRunnerTest {
    @Test
    void shouldThrowUnsupportedIfBuildFileAvailable() throws IOException {
        Path tmpDir = null;

        try {
            tmpDir = Files.createTempDirectory("prepareCommandGradle");
            Files.createFile(tmpDir.resolve("build.gradle"));
            var opt = GradleRunner.forProject(tmpDir.toFile());
            assertTrue(opt.isPresent());
            var runner = opt.orElseThrow();
            var e = assertThrows(UnsupportedOperationException.class,
                    () -> runner.run(null));
            assertNull(e.getCause());
            assertEquals("Gradle is not supported yet", e.getMessage());
        } finally {
            if (tmpDir != null) {
                Files.deleteIfExists(tmpDir.resolve("build.gradle"));
                Files.deleteIfExists(tmpDir);
            }
        }
    }

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
