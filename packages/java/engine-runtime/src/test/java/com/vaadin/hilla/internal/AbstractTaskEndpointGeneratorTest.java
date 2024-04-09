package com.vaadin.hilla.internal;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.Function;

import com.vaadin.hilla.engine.commandrunner.CommandRunnerException;
import com.vaadin.hilla.engine.commandrunner.MavenRunner;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.server.ExecutionFailedException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class AbstractTaskEndpointGeneratorTest extends TaskTest {
    @Test
    void shouldThrowIfProjectDirectoryIsNull() {
        assertThrowsExactly(NullPointerException.class, () -> {
            new TestTaskEndpointGenerator(null, getBuildDirectory(),
                    getTemporaryDirectory().resolve(getOutputDirectory())
                            .toFile());
        }, "Project directory cannot be null");
    }

    @Test
    void shouldThrowIfBuildDirectoryNameIsNull() {
        assertThrowsExactly(NullPointerException.class, () -> {
            new TestTaskEndpointGenerator(getTemporaryDirectory().toFile(),
                    null, getTemporaryDirectory().resolve(getOutputDirectory())
                            .toFile());
        }, "Build directory name cannot be null");
    }

    @Test
    void shouldThrowIfOutputDirectoryIsNull() {
        assertThrowsExactly(NullPointerException.class, () -> {
            new TestTaskEndpointGenerator(getTemporaryDirectory().toFile(),
                    getBuildDirectory(), null);
        }, "Output directory cannot be null");
    }

    private final Function<String, URL> resourceFinder = Thread.currentThread()
            .getContextClassLoader()::getResource;

    private class TestTaskEndpointGenerator
            extends AbstractTaskEndpointGenerator {
        TestTaskEndpointGenerator(File projectDirectory,
                String buildDirectoryName, File outputDirectory) {
            super(projectDirectory, buildDirectoryName, outputDirectory,
                    resourceFinder);
        }

        @Override
        public void execute() throws ExecutionFailedException {
            // no-op
        }
    }

    private class MockedTaskEndpointGenerator
            extends AbstractTaskEndpointGenerator {
        MockedTaskEndpointGenerator(File projectDirectory,
                String buildDirectoryName, File outputDirectory) {
            super(projectDirectory, buildDirectoryName, outputDirectory,
                    resourceFinder);
        }

        @Override
        public void execute() throws ExecutionFailedException {
            getEngineConfiguration();
        }
    }

    @Test
    void shouldRunSpecificVersionWhenGoalIsNotFound() throws Exception {
        var mockConfigureNotFound = mock(MavenRunner.class);
        var rootException = new RuntimeException(
                "MOCK: Could not find goal 'configure' in plugin");
        doThrow(new CommandRunnerException("MOCK: Failed to execute command",
                rootException)).when(mockConfigureNotFound).run(null, false);
        var mockConfigureFound = mock(MavenRunner.class);

        try (var staticMock = mockStatic(MavenRunner.class)) {
            staticMock
                    .when(() -> MavenRunner.forProject(any(), eq("-q"),
                            eq("vaadin:configure")))
                    .thenReturn(Optional.of(mockConfigureNotFound));
            staticMock
                    .when(() -> MavenRunner.forProject(any(), eq("-q"), eq(
                            "com.vaadin:vaadin-maven-plugin:1.0.0:configure")))
                    .thenReturn(Optional.of(mockConfigureFound));
            var task = new MockedTaskEndpointGenerator(
                    getTemporaryDirectory().toFile(), getBuildDirectory(),
                    getTemporaryDirectory().resolve(getOutputDirectory())
                            .toFile());
            var firstRun = AbstractTaskEndpointGenerator.class
                    .getDeclaredField("firstRun");
            firstRun.setAccessible(true);
            firstRun.set(null, true);
            Files.createFile(getTemporaryDirectory().resolve("pom.xml"));
            task.execute();
            verify(mockConfigureNotFound).run(null, false);
            verify(mockConfigureFound).run(null, false);
        }
    }
}
