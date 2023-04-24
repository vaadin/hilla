package dev.hilla.engine;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import dev.hilla.engine.commandrunner.CommandRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class GeneratorShellRunner implements CommandRunner {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(GeneratorShellRunner.class);

    private final File rootDirectory;
    private final String nodeCommand;
    private final String[] arguments;

    public GeneratorShellRunner(File rootDirectory, String nodeCommand,
            String... arguments) {
        this.rootDirectory = rootDirectory;
        this.nodeCommand = nodeCommand;
        this.arguments = arguments;
    }

    @Override
    public String[] testArguments() {
        return new String[] { "-v" };
    }

    @Override
    public String[] arguments() {
        return arguments;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public File currentDirectory() {
        return rootDirectory;
    }

    @Override
    public List<String> executables() {
        return nodeCommand == null ? List.of("node")
                : List.of(nodeCommand, "node");
    }
}
