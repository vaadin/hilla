package dev.hilla.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.server.ExecutionFailedException;

class TaskGenerateHillaImplTest {
    private static final File DIR = new File(
            System.getProperty("user.dir", "."));

    @Test
    void shouldThrowExeptionIfNotConfigured() {
        var gen = new TaskGenerateHillaImpl();
        var e = assertThrowsExactly(ExecutionFailedException.class,
                () -> gen.execute());
        assertEquals("Project directory not set", e.getMessage());
    }

    @Test
    void executeShouldBeAbleToListFilesInProjectDir() {
        var gen = new TaskGenerateHillaImpl() {
            @Override
            List<String> prepareCommand() {
                return List.of(TaskGenerateHillaImpl.MAVEN_COMMAND, "-v");
            }
        };

        try {
            gen.configure(DIR, null);
            gen.execute();
        } catch (ExecutionFailedException e) {
            fail("Shouldn't have thrown an exception", e);
        }
    }

    @Test
    void executeShouldNotCatchExecutionFailedException() {
        final var cmd = "not-a-real-command";
        final var errorMessage = "Generated error";

        var gen = new TaskGenerateHillaImpl() {
            @Override
            void runCodeGeneration(List<String> command)
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
            gen.configure(DIR, null);
            gen.execute();
            fail("Should have thrown exception");
        } catch (ExecutionFailedException e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }

    @Test
    void runCodeGenerationShouldExecuteMaven() throws ExecutionFailedException {
        var gen = new TaskGenerateHillaImpl();
        gen.runCodeGeneration(
                List.of(TaskGenerateHillaImpl.MAVEN_COMMAND, "-v"));
    }

    @Test
    void runCodeGenerationShouldThrowExceptionForUnknownCommands() {
        var gen = new TaskGenerateHillaImpl();

        try {
            gen.runCodeGeneration(List.of("unknownEvilCommmand"));
            fail("Should throw exception for a shell command that doesn't exist");
        } catch (ExecutionFailedException e) {
            assertNotNull(e.getCause());
            assertFalse(e.getMessage().contains("exit"));
        }
    }

    @Test
    void runCodeGenerationShouldThrowExceptionForBadExitCode() {
        var gen = new TaskGenerateHillaImpl();

        try {
            gen.runCodeGeneration(List.of(TaskGenerateHillaImpl.MAVEN_COMMAND,
                    "thisOptionDoesNotExist"));
            fail("Should throw exception for a shell command that exits with error code");
        } catch (ExecutionFailedException e) {
            assertNull(e.getCause());
            assertTrue(e.getMessage().contains("exit"));
        }
    }

    @Test
    void prepareCommandShouldAcceptMaven() throws IOException {
        Path tmpDir = null;

        try {
            tmpDir = Files.createTempDirectory("prepareCommandMaven");
            Files.createFile(tmpDir.resolve("pom.xml"));
            var gen = new TaskGenerateHillaImpl();
            gen.configure(tmpDir.toFile(), null);
            var command = gen.prepareCommand();
            assertFalse(command.isEmpty());
            assertEquals(TaskGenerateHillaImpl.MAVEN_COMMAND, command.get(0));
        } finally {
            if (tmpDir != null) {
                Files.deleteIfExists(tmpDir.resolve("pom.xml"));
                Files.deleteIfExists(tmpDir);
            }
        }
    }

    @Test
    void prepareCommandShouldThrowGradleUnsupported() throws IOException {
        Path tmpDir = null;

        try {
            tmpDir = Files.createTempDirectory("prepareCommandMaven");
            Files.createFile(tmpDir.resolve("build.gradle"));
            var gen = new TaskGenerateHillaImpl();
            gen.configure(tmpDir.toFile(), null);

            try {
                gen.prepareCommand();
                fail("Should throw exception");
            } catch (UnsupportedOperationException e) {
                assertTrue(e.getMessage().contains("Gradle"));
            }

            final var dummy = "dummy";

            var genNoException = new TaskGenerateHillaImpl() {
                @Override
                List<String> prepareGradleCommand() {
                    return List.of(dummy);
                }
            };

            genNoException.configure(tmpDir.toFile(), null);
            var command = genNoException.prepareCommand();
            assertEquals(1, command.size());
            assertEquals(dummy, command.get(0));
        } finally {
            if (tmpDir != null) {
                Files.deleteIfExists(tmpDir.resolve("build.gradle"));
                Files.deleteIfExists(tmpDir);
            }
        }
    }

    @Test
    void prepareCommandShouldThrowExceptionForUnknownProjectType()
            throws IOException {
        Path tmpDir = null;

        try {
            tmpDir = Files.createTempDirectory("prepareCommandMaven");
            var gen = new TaskGenerateHillaImpl();
            gen.configure(tmpDir.toFile(), null);

            try {
                gen.prepareCommand();
                fail("Should throw exception");
            } catch (IllegalStateException e) {
                assertTrue(e.getMessage().contains("Failed"));
            }
        } finally {
            if (tmpDir != null) {
                Files.deleteIfExists(tmpDir);
            }
        }
    }
}
