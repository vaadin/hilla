package dev.hilla.internal.runner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MavenRunnerTest {

    private static final Path CURRENT = Path.of(".");

    @Test
    public void shouldDiscriminateOS() {
        var windows = new MavenRunner(CURRENT, true);
        var other = new MavenRunner(CURRENT, false);

        assertNotEquals(windows.mavenExecutable(), other.mavenExecutable(),
                "Maven command should be different between OS");
        assertNotEquals(windows.wrapperExecutable(), other.wrapperExecutable(),
                "Maven wrapper command should be different between OS");
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void shouldChooseWrapperIfAvailable(boolean onWindows)
            throws IOException {
        Path projectDir = null;

        try {
            projectDir = Files
                    .createTempDirectory("maven-project-with-wrapper");
            var runner = new MavenRunner(projectDir, onWindows);
            var wrapper = runner.wrapperExecutable();
            Files.createFile(projectDir.resolve(wrapper));

            assertEquals(wrapper, runner.chooseExecutable(),
                    "Maven command should be the wrapper if available");
        } finally {
            if (projectDir != null) {
                delete(projectDir);
            }
        }

    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void shouldChooseDefaultCommandIfNoWrapper(boolean onWindows)
            throws IOException {
        Path projectDir = null;

        try {
            projectDir = Files
                    .createTempDirectory("maven-project-without-wrapper");
            var runner = new MavenRunner(projectDir, onWindows);

            assertEquals(runner.mavenExecutable(), runner.chooseExecutable(),
                    "Maven command should be the default if no wrapper is available");
        } finally {
            if (projectDir != null) {
                delete(projectDir);
            }
        }

    }

    @Test
    public void shouldFailWhenNoExecutableAvailable() {
        var runner = new MavenRunner(CURRENT, false);
        runner = spy(runner);
        when(runner.chooseExecutable()).thenReturn("non-existing-command");

        var exception = assertThrows(RunnerException.class, runner::run,
                "Should throw exception when no executable is available");
        assertEquals("Maven not found", exception.getMessage(),
                "Should use the correct message");
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void shouldReturnEmptyWhenNoPom(boolean hasPom) throws IOException {
        Path projectDir = null;

        try {
            projectDir = Files.createTempDirectory("maven-project");

            if (hasPom) {
                Files.createFile(projectDir.resolve("pom.xml"));
            }

            var runner = MavenRunner.forProject(projectDir);
            assertEquals(hasPom, runner.isPresent(),
                    "Should return runner only if pom.xml is present");
        } finally {
            if (projectDir != null) {
                delete(projectDir);
            }
        }
    }

    private static boolean delete(Path directory) throws IOException {
        return Files.walk(directory).sorted(Comparator.reverseOrder())
                .map(Path::toFile).map(File::delete)
                .allMatch(Boolean.TRUE::equals);
    }
}
