package dev.hilla.engine;

import dev.hilla.parser.utils.ConfigList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public final class GeneratorProcessor {
    private final Path baseDir;
    private static final Logger logger = LoggerFactory
            .getLogger(GeneratorProcessor.class);
    private String input;
    private File outputDir;
    private final GeneratorConfiguration.PluginsProcessor pluginsProcessor =
        new GeneratorConfiguration.PluginsProcessor();

    public GeneratorProcessor(Path baseDir) {
        this.baseDir = baseDir;
        this.outputDir = new File(baseDir.toFile(), "frontend/generated");
    }

    public GeneratorProcessor apply(GeneratorConfiguration generatorConfiguration) {
        if (generatorConfiguration == null) {
            return this;
        }

        generatorConfiguration.getPlugins().ifPresent(this::applyPlugins);
        return this;
    }

    public GeneratorProcessor input(@Nonnull String input) {
        this.input = Objects.requireNonNull(input);
        return this;
    }

    public GeneratorProcessor outputDir(@Nonnull File outputDir) {
        this.outputDir = Objects.requireNonNull(outputDir);
        return this;
    }

    public void process() throws IOException, InterruptedException {
        var runner = new GeneratorShellRunner(baseDir.toFile());
        prepareOutputDir(runner);
        preparePlugins(runner);
        prepareVerbose(runner);
        runner.run(input);
    }

    private void prepareOutputDir(GeneratorShellRunner runner) {
        var outputDirPath = outputDir.toPath();
        var result = outputDirPath.isAbsolute() ? outputDirPath
                : baseDir.resolve(outputDirPath);
        runner.add("-o", result.toString());
    }

    private void applyPlugins(
        @Nonnull GeneratorConfiguration.Plugins plugins) {
        pluginsProcessor.setConfig(plugins);
    }

    private void preparePlugins(GeneratorShellRunner runner) {
        pluginsProcessor.process().stream()
            .map(GeneratorConfiguration.Plugin::getPath).distinct()
            .forEachOrdered(path -> runner.add("-p", path));
    }

    private void prepareVerbose(GeneratorShellRunner runner) {
        if (logger.isDebugEnabled()) {
            runner.add("-v");
        }
    }
}
