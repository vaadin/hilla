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
package com.vaadin.hilla.engine.commandrunner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class GradleRunnerTest {

    @TempDir
    Path tmpDir;

    @ParameterizedTest
    @ValueSource(strings = { "build.gradle", "build.gradle.kts" })
    void shouldRunIfBuildFileIsAvailable(String buildFile) throws IOException {
        Files.createFile(tmpDir.resolve(buildFile));
        var opt = GradleRunner.forProject(tmpDir.toFile(), "-v");
        assertTrue(opt.isPresent());
        var runner = opt.orElseThrow();
        assertEquals(1, runner.arguments().length);
        assumeTrue(isGradleAvailable(), "Gradle is not available");
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

    private static boolean isGradleAvailable() {
        if (System.getProperty(GradleRunner.EXECUTABLE_PROPERTY) != null) {
            return true;
        }
        var executables = CommandRunner.IS_WINDOWS
                ? List.of("gradle.bat", "gradle.cmd", "gradle.exe", "gradle")
                : List.of("gradle");
        return Optional.ofNullable(System.getenv("PATH")).stream()
                .flatMap(path -> Stream.of(path.split(File.pathSeparator)))
                .flatMap(dir -> executables.stream()
                        .map(executable -> new File(dir, executable)))
                .anyMatch(File::canExecute);
    }
}
