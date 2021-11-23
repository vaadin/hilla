package com.vaadin.fusion.maven.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

public class GeneratorShellRunner {
    private final List<String> arguments = new ArrayList<>();
    private final Log logger;

    public GeneratorShellRunner(List<String> executable, Log logger) {
        this.logger = logger;

        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            arguments.add("cmd.exe");
            arguments.add("/c");
        } else {
            arguments.add("bash");
            arguments.add("-c");
        }

        arguments.addAll(executable);
    }

    public void add(String... args) {
        arguments.addAll(List.of(args));
    }

    public void run() throws InterruptedException, IOException {
        logger.debug(String.format("Executing command: %s",
                String.join(" ", arguments)));

        var builder = new ProcessBuilder().command(arguments).inheritIO();

        var process = builder.start();

        var exitCode = process.waitFor();

        if (exitCode == 0) {
            logger.info("The Generator process finished with the exit code "
                    + exitCode);
        } else {
            throw new GeneratorException(
                    "Generator execution failed with exit code " + exitCode);
        }
    }
}
