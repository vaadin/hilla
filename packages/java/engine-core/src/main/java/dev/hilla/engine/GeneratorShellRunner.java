package dev.hilla.engine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.frontend.FrontendUtils;

final class GeneratorShellRunner {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(GeneratorShellRunner.class);
    private static final Path TSGEN_PATH = Paths.get("node_modules", "@hilla",
            "generator-typescript-cli", "bin", "index.js");
    private final List<String> arguments = new ArrayList<>();
    private final File rootDirectory;

    public GeneratorShellRunner(File rootDirectory, String nodeCommand) {
        this.rootDirectory = rootDirectory;
        arguments.add(nodeCommand);
        arguments.add(TSGEN_PATH.toString());
    }

    public void add(String... args) {
        arguments.addAll(List.of(args));
    }

    public void run(String input) throws InterruptedException, IOException {
        Objects.requireNonNull(input);
        List<String> args;

        if (FrontendUtils.isWindows()) {
            args = List.of("cmd.exe", "/c", arguments.stream()
                    .map(arg -> (arg.contains(" ") ? "\"" + arg + "\"" : arg))
                    .collect(Collectors.joining(" ", "\"", "\"")));
        } else {
            args = arguments;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Executing command: {}", String.join(" ", args));
        }

        var builder = new ProcessBuilder().directory(rootDirectory)
                .command(args).redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT);

        var process = builder.start();

        try (var stdin = process.getOutputStream()) {
            stdin.write(input.getBytes(StandardCharsets.UTF_8));
        }

        var exitCode = process.waitFor();

        if (exitCode == 0) {
            LOGGER.info("The Generator process finished with the exit code {}",
                    exitCode);
        } else {
            throw new GeneratorException(
                    "Generator execution failed with exit code " + exitCode);
        }
    }
}
