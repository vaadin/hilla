package com.vaadin.hilla.engine.commandrunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MavenRunnerTest {

    @TempDir
    Path tmpDir;

    @Test
    void shouldRunIfPOMAvailable() throws IOException {
        Files.createFile(tmpDir.resolve("pom.xml"));
        var opt = MavenRunner.forProject(tmpDir.toFile(), "-v");
        assertTrue(opt.isPresent());
        var runner = opt.orElseThrow();
        assertEquals(1, runner.arguments().length);
        assertDoesNotThrow(() -> runner.run(null));
    }

    @Test
    void shouldNotCreateRunnerForUnknownProjectType() {
        var runner = MavenRunner.forProject(tmpDir.toFile());
        assertTrue(runner.isEmpty());
    }

    @Test
    void shouldListProvidedExecutable() {
        String originalValue = System
                .getProperty(MavenRunner.EXECUTABLE_PROPERTY);
        try {
            String customMavenPath = "/path/to/maven/bin/mvn";
            System.setProperty(MavenRunner.EXECUTABLE_PROPERTY,
                    customMavenPath);
            var runner = new MavenRunner(tmpDir.toFile(), "-v");
            if (CommandRunner.IS_WINDOWS) {
                assertEquals(List.of(customMavenPath, ".\\mvnw.cmd", "mvn.cmd"),
                        runner.executables());
            } else {
                assertEquals(List.of(customMavenPath, "./mvnw", "mvn"),
                        runner.executables());
            }
        } finally {
            if (originalValue != null) {
                System.setProperty(MavenRunner.EXECUTABLE_PROPERTY,
                        originalValue);
            } else {
                System.clearProperty(MavenRunner.EXECUTABLE_PROPERTY);
            }
        }
    }
}
