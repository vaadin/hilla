package dev.hilla.internal;

import com.vaadin.flow.server.ExecutionFailedException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskGenerateHillaImplTest {
    private static final String DIR = System.getProperty("user.dir", ".");

    @Test
    void executeShouldBeAbleToListFilesInProjectDir() {
        var gen = new TaskGenerateHillaImpl() {
            @Override
            List<String> prepareCommand() {
                return List.of("ls", DIR);
            }
        };

        try {
            gen.execute();
        } catch (ExecutionFailedException e) {
            fail("Shouldn't have thrown an exception", e);
        }
    }

    @Test
    void executeShouldNotCatchExecutionFailedException() {
        final var dir = System.getProperty("user.dir", ".");
        final var errorMessage = "Generated error";

        var gen = new TaskGenerateHillaImpl() {
            @Override
            void runCodeGeneration(List<String> command)
                    throws ExecutionFailedException {
                assertEquals(1, command.size());
                assertEquals(dir, command.get(0));
                throw new ExecutionFailedException(errorMessage);
            }

            @Override
            List<String> prepareCommand() {
                return List.of(DIR);
            }
        };

        try {
            gen.execute();
            fail("Should have thrown exception");
        } catch (ExecutionFailedException e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }

    @Test
    void runCodeGenerationShouldExecuteMaven() throws ExecutionFailedException {
        var gen = new TaskGenerateHillaImpl();
        gen.runCodeGeneration(List.of("mvn", "-v"));
    }

    @Test
    void runCodeGenerationShouldThrowExceptionForUnknownCommands() {
        var gen = new TaskGenerateHillaImpl();

        try {
            gen.runCodeGeneration(List.of("unknownEvilCommmand"));
            fail("Should throw exception for a shell command that doesn't exist");
        } catch (ExecutionFailedException ex) {
            assertNotNull(ex.getCause());
            assertFalse(ex.getMessage().contains("exit"));
        }
    }

    @Test
    void runCodeGenerationShouldThrowExceptionForBadExitCode() {
        var gen = new TaskGenerateHillaImpl();

        try {
            gen.runCodeGeneration(List.of("ls", "thisDirectoryShouldNotExist"));
            fail("Should throw exception for a shell command that exits with error code");
        } catch (ExecutionFailedException ex) {
            assertNull(ex.getCause());
            assertTrue(ex.getMessage().contains("exit"));
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
            assertEquals(2, command.size());
            assertEquals("mvn", command.get(0));
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
            } catch (UnsupportedOperationException ex) {
                assertTrue(ex.getMessage().contains("Gradle"));
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
            } catch (IllegalStateException ex) {
                assertTrue(ex.getMessage().contains("Failed"));
            }
        } finally {
            if (tmpDir != null) {
                Files.deleteIfExists(tmpDir);
            }
        }
    }
}
