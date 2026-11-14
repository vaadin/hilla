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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class GradleRunnerTest {

    private static final String INVOCATIONS_FILE = "invocations.txt";

    @TempDir
    Path tmpDir;

    @ParameterizedTest
    @ValueSource(strings = { "build.gradle", "build.gradle.kts" })
    void shouldRunIfBuildFileIsAvailable(String buildFile) throws IOException {
        Files.createFile(tmpDir.resolve(buildFile));
        createGradleWrapperStub();
        var opt = GradleRunner.forProject(tmpDir.toFile(), "build");
        assertTrue(opt.isPresent());
        var runner = opt.orElseThrow();
        assertEquals(1, runner.arguments().length);
        // A custom executable would be tried before the project's wrapper
        String originalValue = System
                .clearProperty(GradleRunner.EXECUTABLE_PROPERTY);
        try {
            assertDoesNotThrow(() -> runner.run(null));
        } finally {
            if (originalValue != null) {
                System.setProperty(GradleRunner.EXECUTABLE_PROPERTY,
                        originalValue);
            }
        }
        // The wrapper is first tested with "-v", then run with the arguments
        assertEquals(List.of("-v", "build"),
                Files.readAllLines(tmpDir.resolve(INVOCATIONS_FILE)).stream()
                        .map(String::strip).toList());
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
                System.clearProperty(GradleRunner.EXECUTABLE_PROPERTY);
            }
        }
    }

    /**
     * Creates a Gradle wrapper stub in the project directory that only records
     * the arguments it is called with, so that the runner can be tested without
     * a Gradle installation.
     */
    private void createGradleWrapperStub() throws IOException {
        if (CommandRunner.IS_WINDOWS) {
            Files.writeString(tmpDir.resolve("gradlew.bat"),
                    "@echo %*>> " + INVOCATIONS_FILE + "\r\n");
        } else {
            var wrapper = tmpDir.resolve("gradlew");
            Files.writeString(wrapper,
                    "#!/bin/sh\necho \"$@\" >> " + INVOCATIONS_FILE + "\n");
            assertTrue(wrapper.toFile().setExecutable(true));
        }
    }
}
