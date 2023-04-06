package dev.hilla.engine.commandrunner;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class MavenRunnerTest {
    @Test
    void shouldRunIfPOMAvailable() throws IOException {
        Path tmpDir = null;

        try {
            tmpDir = Files.createTempDirectory("prepareCommandMaven");
            Files.createFile(tmpDir.resolve("pom.xml"));
            var opt = MavenRunner.forProject(tmpDir.toFile(), "-v");
            assertTrue(opt.isPresent());
            var runner = opt.orElseThrow();
            assertEquals(1, runner.arguments().length);
            assertDoesNotThrow(() -> runner.run(null));
        } finally {
            if (tmpDir != null) {
                Files.deleteIfExists(tmpDir.resolve("pom.xml"));
                Files.deleteIfExists(tmpDir);
            }
        }
    }

    @Test
    void shouldNotCreateRunnerForUnknownProjectType() throws IOException {
        Path tmpDir = null;

        try {
            tmpDir = Files.createTempDirectory("prepareCommandMaven");
            var runner = MavenRunner.forProject(tmpDir.toFile());
            assertTrue(runner.isEmpty());
        } finally {
            if (tmpDir != null) {
                Files.deleteIfExists(tmpDir);
            }
        }
    }
}
