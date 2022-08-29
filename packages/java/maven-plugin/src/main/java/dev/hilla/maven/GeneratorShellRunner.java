package dev.hilla.maven;

import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class GeneratorShellRunner {
    private static final boolean IS_WINDOWS;
    private static final String TSGEN;
    private static final Pattern JSON_ESCAPE_PATTERN = Pattern
            .compile("[\r\n\b\f\t\"']");

    static {
        var osName = System.getProperty("os.name").toLowerCase();
        IS_WINDOWS = osName.contains("windows");
        TSGEN = IS_WINDOWS ? "tsgen.cmd" : "tsgen";
    }

    private final List<String> arguments = new ArrayList<>();
    private String openAPI;
    private final Log logger;

    public GeneratorShellRunner(File baseDir, Log logger) {
        this.logger = logger;

        if (IS_WINDOWS) {
            arguments.add("cmd.exe");
            arguments.add("/c");
        }

        arguments.add(Paths.get("node_modules", ".bin", TSGEN).toString());
    }

    public void setOpenAPI(String json) {
        openAPI = (IS_WINDOWS ? JSON_ESCAPE_PATTERN.matcher(json)
                .replaceAll(match -> "\\\\" + match.group(0)) : json);
    }

    public void add(String... args) {
        arguments.addAll(List.of(args));
    }

    public void run() throws InterruptedException, IOException {
        if (openAPI == null) {
            throw new IllegalStateException(
                    "`setOpenAPI` should have been called before");
        }

        logger.debug(String.format("Executing command: %s",
                String.join(" ", arguments)));

        var builder = new ProcessBuilder().command(arguments)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT);

        var process = builder.start();

        try (var stdin = process.getOutputStream()) {
            stdin.write(openAPI.getBytes(StandardCharsets.UTF_8));
        }

        var exitCode = process.waitFor();

        if (exitCode == 0) {
            logger.info("The Generator process finished with the exit code "
                    + exitCode);
        } else {
            throw new GeneratorException(
                    "Generator execution failed with exit code " + exitCode);
        }
    }

    public void runNpmInstall() throws InterruptedException, IOException {
        var builder = new ProcessBuilder().command(List.of("npm", "install"))
                .inheritIO();

        var process = builder.start();

        var exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new GeneratorException(
                    "`npm install` failed with exit code " + exitCode);
        }
    }
}
