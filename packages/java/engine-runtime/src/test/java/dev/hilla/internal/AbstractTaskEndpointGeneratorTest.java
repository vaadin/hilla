package dev.hilla.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import dev.hilla.engine.EngineConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.server.ExecutionFailedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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

    @Test
    void executeShouldBeAbleToListFilesInProjectDir() {
        var task = new TestTaskEndpointGenerator(
                getTemporaryDirectory().toFile(), getBuildDirectory(),
                getTemporaryDirectory().resolve(getOutputDirectory())
                        .toFile()) {
            @Override
            List<String> prepareCommand() {
                return List.of(AbstractTaskEndpointGenerator.MAVEN_COMMAND,
                        "-v");
            }
        };

        try {
            task.getEngineConfiguration();
        } catch (ExecutionFailedException e) {
            fail("Shouldn't have thrown an exception", e);
        }
    }

    @Test
    void executeShouldNotCatchExecutionFailedException() {
        final var cmd = "not-a-real-command";
        final var errorMessage = "Generated error";

        var task = new TestTaskEndpointGenerator(
                getTemporaryDirectory().toFile(), getBuildDirectory(),
                getTemporaryDirectory().resolve(getOutputDirectory())
                        .toFile()) {
            @Override
            void runConfigure(List<String> command)
                    throws ExecutionFailedException {
                assertEquals(1, command.size());
                assertEquals(cmd, command.get(0));
                throw new ExecutionFailedException(errorMessage);
            }

            @Override
            List<String> prepareCommand() {
                return List.of(cmd);
            }
        };

        try {
            task.getEngineConfiguration();
            fail("Should have thrown exception");
        } catch (ExecutionFailedException e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }

    @Test
    void runConfigureShouldExecuteMaven() throws ExecutionFailedException {
        var task = new TestTaskEndpointGenerator(
                getTemporaryDirectory().toFile(), getBuildDirectory(),
                getTemporaryDirectory().resolve(getOutputDirectory()).toFile());
        task.runConfigure(
                List.of(AbstractTaskEndpointGenerator.MAVEN_COMMAND, "-v"));
    }

    @Test
    void runConfigureShouldThrowExceptionForUnknownCommands() {
        var task = new TestTaskEndpointGenerator(
                getTemporaryDirectory().toFile(), getBuildDirectory(),
                getTemporaryDirectory().resolve(getOutputDirectory()).toFile());

        try {
            task.runConfigure(List.of("unknownEvilCommmand"));
            fail("Should throw exception for a shell command that doesn't exist");
        } catch (ExecutionFailedException e) {
            assertNotNull(e.getCause());
            assertFalse(e.getMessage().contains("exit"));
        }
    }

    @Test
    void runCodeGenerationShouldThrowExceptionForBadExitCode() {
        var task = new TestTaskEndpointGenerator(
                getTemporaryDirectory().toFile(), getBuildDirectory(),
                getTemporaryDirectory().resolve(getOutputDirectory()).toFile());

        try {
            task.runConfigure(
                    List.of(AbstractTaskEndpointGenerator.MAVEN_COMMAND,
                            "thisOptionDoesNotExist"));
            fail("Should throw exception for a shell command that exits with error code");
        } catch (ExecutionFailedException e) {
            assertNull(e.getCause());
            assertTrue(e.getMessage().contains("exit"));
        }
    }

    @Test
    void prepareCommandShouldAcceptMaven() throws IOException {
        var tmpDir = getTemporaryDirectory();

        try {
            Files.createFile(tmpDir.resolve("pom.xml"));
            var task = new TestTaskEndpointGenerator(
                    getTemporaryDirectory().toFile(), getBuildDirectory(),
                    getTemporaryDirectory().resolve(getOutputDirectory())
                            .toFile());
            var command = task.prepareCommand();
            assertFalse(command.isEmpty());
            assertEquals(AbstractTaskEndpointGenerator.MAVEN_COMMAND,
                    command.get(0));
        } finally {
            Files.deleteIfExists(tmpDir.resolve("pom.xml"));
        }
    }

    @Test
    void prepareCommandShouldThrowGradleUnsupported() throws IOException {
        var tmpDir = getTemporaryDirectory();

        try {
            Files.createFile(tmpDir.resolve("build.gradle"));
            var task = new TestTaskEndpointGenerator(
                    getTemporaryDirectory().toFile(), getBuildDirectory(),
                    getTemporaryDirectory().resolve(getOutputDirectory())
                            .toFile());

            try {
                task.prepareCommand();
                fail("Should throw exception");
            } catch (UnsupportedOperationException e) {
                assertTrue(e.getMessage().contains("Gradle"));
            }

            final var dummy = "dummy";

            var taskNoException = new TestTaskEndpointGenerator(
                    getTemporaryDirectory().toFile(), getBuildDirectory(),
                    getTemporaryDirectory().resolve(getOutputDirectory())
                            .toFile()) {
                @Override
                List<String> prepareGradleCommand() {
                    return List.of(dummy);
                }
            };

            var command = taskNoException.prepareCommand();
            assertEquals(1, command.size());
            assertEquals(dummy, command.get(0));
        } finally {
            Files.deleteIfExists(tmpDir.resolve("build.gradle"));
        }
    }

    @Test
    void prepareCommandShouldThrowExceptionForUnknownProjectType()
            throws IOException {
        var tmpDir = getTemporaryDirectory();

        var task = new TestTaskEndpointGenerator(
                getTemporaryDirectory().toFile(), getBuildDirectory(),
                getTemporaryDirectory().resolve(getOutputDirectory()).toFile());

        try {
            task.prepareCommand();
            fail("Should throw exception");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Failed"));
        }
    }

    /**
     * Removes hilla-engine-configuration.json created in
     * {@link TaskTest#setUpTaskApplication()}
     */
    @BeforeEach
    public void removeStoredConfigurationFile() throws IOException {
        var buildDir = getTemporaryDirectory().resolve(getBuildDirectory());
        var configFile = buildDir.resolve(EngineConfiguration.RESOURCE_NAME);
        Files.delete(configFile);
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
