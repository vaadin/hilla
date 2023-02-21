package dev.hilla.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class GeneratorShellRunner {
    private static final boolean IS_WINDOWS;
    private static final String TSGEN;

    static {
        var osName = System.getProperty("os.name").toLowerCase();
        IS_WINDOWS = osName.contains("windows");
        TSGEN = IS_WINDOWS ? "tsgen.cmd" : "tsgen";
    }

    private final List<String> arguments = new ArrayList<>();

    private static final Logger logger = LoggerFactory
            .getLogger(GeneratorShellRunner.class);

    private final File workingDirectory;

    public GeneratorShellRunner(File workingDirectory) {
        this.workingDirectory = workingDirectory;

        if (IS_WINDOWS) {
            arguments.add("cmd.exe");
            arguments.add("/c");
        }

        var tsgenPath = Paths.get("node_modules", ".bin", TSGEN);
        arguments.add(tsgenPath.toString());
    }

    public void add(String... args) {
        arguments.addAll(List.of(args));
    }

    public void run(String input) throws InterruptedException, IOException {
        Objects.requireNonNull(input);

        if (logger.isDebugEnabled()) {
            logger.debug("Executing command: {}", String.join(" ", arguments));
        }

        var builder = new ProcessBuilder().directory(workingDirectory)
                .command(arguments)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT);

        var process = builder.start();

        try (var stdin = process.getOutputStream()) {
            stdin.write(input.getBytes(StandardCharsets.UTF_8));
        }

        var exitCode = process.waitFor();

        if (exitCode == 0) {
            logger.info("The Generator process finished with the exit code {}",
                    exitCode);
        } else {
            throw new GeneratorException(
                    "Generator execution failed with exit code " + exitCode);
        }
    }
}
