package dev.hilla.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public final class GeneratorProcessor {
    private static final List<GeneratorConfiguration.Plugin> DEFAULT_PLUGINS = Arrays
            .asList(new GeneratorConfiguration.Plugin(
                    "@hilla/generator-typescript-plugin-client"),
                    new GeneratorConfiguration.Plugin(
                            "@hilla/generator-typescript-plugin-backbone"),
                    new GeneratorConfiguration.Plugin(
                            "@hilla/generator-typescript-plugin-barrel"),
                    new GeneratorConfiguration.Plugin(
                            "@hilla/generator-typescript-plugin-model"),
                    new GeneratorConfiguration.Plugin(
                            "@hilla/generator-typescript-plugin-push"));

    private final Path baseDir;
    private static final Logger logger = LoggerFactory
            .getLogger(GeneratorProcessor.class);
    private String input;
    private File outputDir;
    private Set<GeneratorConfiguration.Plugin> plugins = new LinkedHashSet<>(
            DEFAULT_PLUGINS);

    public GeneratorProcessor(Path baseDir) {
        this.baseDir = baseDir;
        this.outputDir = new File(baseDir.toFile(), "frontend/generated");
    }

    public GeneratorProcessor input(@Nonnull String input) {
        this.input = Objects.requireNonNull(input);
        return this;
    }

    public GeneratorProcessor outputDir(@Nonnull File outputDir) {
        this.outputDir = Objects.requireNonNull(outputDir);
        return this;
    }

    public GeneratorProcessor plugins(
            @Nonnull GeneratorConfiguration.PluginList plugins) {
        var pluginStream = Objects.requireNonNull(plugins).getUse().stream();

        if (!plugins.isDisableAllDefaults()) {
            pluginStream = Stream.concat(
                    this.plugins.stream().filter(
                            plugin -> !plugins.getDisable().contains(plugin)),
                    pluginStream);
        }

        this.plugins = pluginStream
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return this;
    }

    public void process() throws IOException, InterruptedException,
            GeneratorUnavailableException {
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

    private void preparePlugins(GeneratorShellRunner runner) {
        plugins.stream().map(GeneratorConfiguration.Plugin::getPath).distinct()
                .forEach(path -> runner.add("-p", path));
    }

    private void prepareVerbose(GeneratorShellRunner runner) {
        if (logger.isDebugEnabled()) {
            runner.add("-v");
        }
    }
}
