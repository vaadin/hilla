package com.vaadin.hilla.engine.commandrunner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class CommandRunnerTest {

    static final Logger LOGGER = LoggerFactory
            .getLogger(CommandRunnerTest.class);

    static final String USER_DIR = System.getProperty("user.dir", ".");
    static final String DIR_LS = CommandRunner.IS_WINDOWS ? "dir" : "ls";

    abstract static class TestRunner implements CommandRunner {

        @Override
        public Logger getLogger() {
            return LOGGER;
        }

        @Override
        public File currentDirectory() {
            return new File(USER_DIR);
        }

        @Override
        public String[] testArguments() {
            return new String[0];
        }

        @Override
        public String[] arguments() {
            return new String[0];
        }
    }

    @Test
    void shouldBeAbleToListFilesInProjectDir() {
        var runner = new TestRunner() {

            @Override
            public List<String> executables() {
                return List.of(DIR_LS);
            }
        };

        assertDoesNotThrow(() -> runner.run(null));
    }

    @Test
    void shouldPrintOutputFromCommand() {
        var originalOut = System.out;
        var outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            var runner = new TestRunner() {
                @Override
                public List<String> executables() {
                    return List.of(DIR_LS);
                }
            };

            assertDoesNotThrow(() -> runner.run(null));
            assertTrue(outContent.toString().contains("pom.xml"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void shouldPrintErrorFromCommand() {
        var originalErr = System.err;
        var errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try {
            var runner = new TestRunner() {
                @Override
                public List<String> executables() {
                    return List.of(DIR_LS);
                }

                @Override
                public String[] arguments() {
                    return new String[] { "thisDirectoryShouldNotExist" };
                }
            };

            assertThrows(CommandRunnerException.class, () -> runner.run(null));
            // On Windows, the error message does not necessarily contain the
            // name of the missing directory
            assertFalse(errContent.toString().isEmpty());
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void shouldThrowExceptionForUnknownCommands() {
        var runner = new TestRunner() {

            @Override
            public List<String> executables() {
                return List.of("unknownCommand");
            }
        };

        var e = assertThrows(CommandNotFoundException.class,
                () -> runner.run(null));
        assertNull(e.getCause());
        assertFalse(e.getMessage().contains("exit"));
        assertTrue(e.getMessage().contains("unknownCommand"));
    }

    @Test
    void runCodeGenerationShouldThrowExceptionForBadExitCode() {
        var runner = new TestRunner() {

            @Override
            public String[] arguments() {
                return new String[] { "thisDirectoryShouldNotExist" };
            }

            @Override
            public List<String> executables() {
                return List.of(DIR_LS);
            }
        };

        var e = assertThrows(CommandRunnerException.class,
                () -> runner.run(null));
        assertNull(e.getCause());
        assertTrue(e.getMessage().contains("exit"));
    }

    @Test
    void shouldChooseTheExecutableThatWorks() {
        var runner = new TestRunner() {
            @Override
            public List<String> executables() {
                return List.of("fakeCommand", DIR_LS);
            }
        };

        assertDoesNotThrow(() -> runner.run(null));
    }

    static class JavaExecTestArgs implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext ec)
                throws Exception {
            return CommandRunner.IS_WINDOWS ? Stream.of(
                    Arguments.of("c:\\path\\to\\java\\home\\bin\\java.exe",
                            "c:\\path\\to\\java\\home"),
                    Arguments.of("c:\\path\\to\\java\\home\\bin\\javaw.exe",
                            "c:\\path\\to\\java\\home"))
                    : Stream.of(Arguments.of("/path/to/java/home/bin/java",
                            "/path/to/java/home"));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(JavaExecTestArgs.class)
    void runningCommands_javaExecutablePathFromCurrentRunningProcess_isUsedToSetJavaHomeOfProcessBuilder(
            String executable, String expectedJavaHome) {

        TestRunner runner = new TestRunner() {

            @Override
            public List<String> executables() {
                return List.of("fakeCommand", DIR_LS);
            }

            @Override
            public ProcessHandle.Info getCurrentProcessInfo() {
                return new MockProcessInfo();
            }

            class MockProcessInfo implements ProcessHandle.Info {

                @Override
                public Optional<String> command() {
                    return Optional.of(executable);
                }

                @Override
                public Optional<String> commandLine() {
                    return Optional.empty();
                }

                @Override
                public Optional<String[]> arguments() {
                    return Optional.empty();
                }

                @Override
                public Optional<Instant> startInstant() {
                    return Optional.empty();
                }

                @Override
                public Optional<Duration> totalCpuDuration() {
                    return Optional.empty();
                }

                @Override
                public Optional<String> user() {
                    return Optional.empty();
                }
            }
        };

        assertEquals(expectedJavaHome, runner.environment().get("JAVA_HOME"));

        assertEquals(expectedJavaHome,
                runner.createProcessBuilder(List.of(), false).environment()
                        .get("JAVA_HOME"));

        var ProcessBuilder = runner.createProcessBuilder(List.of(), false);
        assertEquals(expectedJavaHome,
                ProcessBuilder.environment().get("JAVA_HOME"));
    }

    @Test
    void run_redirectOutputsToMainProcess() {
        List<Boolean> stdOutRequested = new ArrayList<>();
        var runner = new TestRunner() {

            @Override
            public List<String> executables() {
                return List.of(DIR_LS);
            }

            @Override
            public ProcessBuilder createProcessBuilder(
                    List<String> commandWithArgs, boolean stdOut) {
                stdOutRequested.add(stdOut);
                return super.createProcessBuilder(commandWithArgs, stdOut);
            }
        };

        assertDoesNotThrow(() -> runner.run(null));
        assertEquals(2, stdOutRequested.size());
        assertEquals(false, stdOutRequested.get(0));
        assertEquals(true, stdOutRequested.get(1));
    }

    @Test
    void run_silent_doesNotRedirectOutputsToMainProcess() {
        List<Boolean> stdOutRequested = new ArrayList<>();
        var runner = new TestRunner() {

            @Override
            public List<String> executables() {
                return List.of(DIR_LS);
            }

            @Override
            public ProcessBuilder createProcessBuilder(
                    List<String> commandWithArgs, boolean stdOut) {
                stdOutRequested.add(stdOut);
                return super.createProcessBuilder(commandWithArgs, stdOut);
            }
        };

        assertDoesNotThrow(() -> runner.run(null, false));
        assertEquals(2, stdOutRequested.size());
        assertEquals(false, stdOutRequested.get(0));
        assertEquals(false, stdOutRequested.get(1));
    }

}
