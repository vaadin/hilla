package dev.hilla.engine.commandrunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Runs a Maven command.
 */
public class MavenRunner implements CommandRunner {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MavenRunner.class);

    private final File projectDir;
    private final String[] args;

    /**
     * Creates a Maven runner.
     *
     * @param projectDir
     *            the project directory
     * @param args
     *            the arguments to pass to Maven
     */
    public MavenRunner(File projectDir, String... args) {
        this.projectDir = projectDir;
        this.args = args;
    }

    /**
     * Creates a Maven runner for the given project directory.
     *
     * @param projectDir
     *            the project directory
     * @param args
     *            the arguments to pass to Maven
     * @return a Maven runner if the project directory contains a Maven project,
     *         an empty optional otherwise
     */
    public static Optional<CommandRunner> forProject(File projectDir,
            String... args) {
        if (new File(projectDir, "pom.xml").exists()) {
            return Optional.of(new MavenRunner(projectDir, args));
        }

        return Optional.empty();
    }

    @Override
    public String[] arguments() {
        return args;
    }

    @Override
    public String[] testArguments() {
        return new String[] { "-v" };
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public File currentDirectory() {
        return projectDir;
    }

    @Override
    public List<String> executables() {
        // Prefer the wrapper over a global installation
        return IS_WINDOWS ? List.of(".\\mvnw.cmd", "mvn.cmd")
                : List.of("./mvnw", "mvn");
    }
}
