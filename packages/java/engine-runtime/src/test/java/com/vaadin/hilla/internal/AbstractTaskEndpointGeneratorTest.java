package com.vaadin.hilla.internal;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.server.ExecutionFailedException;

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

    static private class TestTaskEndpointGenerator
            extends AbstractTaskEndpointGenerator {
        TestTaskEndpointGenerator(File projectDirectory,
                String buildDirectoryName, File outputDirectory) {
            super(projectDirectory, buildDirectoryName, outputDirectory);
        }

        @Override
        public void execute() throws ExecutionFailedException {
            // no-op
        }
    }
}
