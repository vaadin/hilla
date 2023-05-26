package dev.hilla.engine.commandrunner;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

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
}
