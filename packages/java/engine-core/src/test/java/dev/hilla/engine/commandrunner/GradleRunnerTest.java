package dev.hilla.engine.commandrunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GradleRunnerTest {

    @TempDir
    Path tmpDir;

    @Test
    void shouldRunIfBuildGradleIsAvailable() throws IOException {
        Files.createFile(tmpDir.resolve("build.gradle"));
        var opt = GradleRunner.forProject(tmpDir.toFile(), "-v");
        assertTrue(opt.isPresent());
        var runner = opt.orElseThrow();
        assertEquals(1, runner.arguments().length);
        assertDoesNotThrow(() -> runner.run(null));
    }

    @Test
    void shouldRunIfBuildGradleKtsIsAvailable() throws IOException {
        Files.createFile(tmpDir.resolve("build.gradle.kts"));
        var opt = GradleRunner.forProject(tmpDir.toFile(), "-v");
        assertTrue(opt.isPresent());
        var runner = opt.orElseThrow();
        assertEquals(1, runner.arguments().length);
        assertDoesNotThrow(() -> runner.run(null));
    }

    @Test
    void shouldNotCreateRunnerForUnknownProjectType() {
        var runner = GradleRunner.forProject(tmpDir.toFile());
        assertTrue(runner.isEmpty());
    }

    @Test
    void shouldListProvidedExecutable() {
        String originalValue = System
                .getProperty(GradleRunner.EXECUTABLE_PROPERTY);
        try {
            String customMavenPath = "/path/to/gradle/bin/gradle";
            System.setProperty(GradleRunner.EXECUTABLE_PROPERTY,
                    customMavenPath);
            var runner = new GradleRunner(tmpDir.toFile(), "-v");
            if (CommandRunner.IS_WINDOWS) {
                assertEquals(List.of(customMavenPath, ".\\gradlew.bat",
                        "gradle.bat", "gradle"), runner.executables());
            } else {
                assertEquals(List.of(customMavenPath, "./gradlew", "gradle"),
                        runner.executables());
            }
        } finally {
            if (originalValue != null) {
                System.setProperty(GradleRunner.EXECUTABLE_PROPERTY,
                        originalValue);
            } else {
                System.clearProperty(MavenRunner.EXECUTABLE_PROPERTY);
            }
        }
    }
}
