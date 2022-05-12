package dev.hilla.maven;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class GeneratorShellRunner {
    private static final Pattern jsonEscapePattern = Pattern
            .compile("[\r\n\b\f\t\"']");

    private final List<String> arguments = new ArrayList<>();
    private final Log logger;

    public GeneratorShellRunner(File baseDir, Log logger) {
        this.logger = logger;

        if (SystemUtils.IS_OS_WINDOWS) {
            arguments.add("cmd.exe");
            arguments.add("/c");
        }

        if (SystemUtils.IS_OS_WINDOWS) {
            arguments.add(Paths.get(baseDir.getAbsolutePath(), "node_modules",
                    ".bin", "tsgen.cmd").toString());
        } else {
            arguments.add(Paths.get(baseDir.getAbsolutePath(), "node_modules",
                    ".bin", "tsgen").toString());
        }
    }

    public static String prepareJSONForCLI(String json) {
        return SystemUtils.IS_OS_WINDOWS ? jsonEscapePattern.matcher(json)
                .replaceAll(match -> "\\\\" + match.group(0)) : json;
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
