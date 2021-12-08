package com.vaadin.fusion.maven;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

final class GeneratorProcessor {
    private final Log logger;
    private final MavenProject project;
    private String input;
    private String outputDir = "frontend/generated";
    private Set<GeneratorConfiguration.Plugin> plugins = new LinkedHashSet<>(3);
    private boolean verbose = false;

    {
        plugins.add(new GeneratorConfiguration.Plugin(
                "@vaadin/generator-typescript-plugin-client"));
        plugins.add(new GeneratorConfiguration.Plugin(
                "@vaadin/generator-typescript-plugin-backbone"));
        plugins.add(new GeneratorConfiguration.Plugin(
                "@vaadin/generator-typescript-plugin-barrel"));
    }

    public GeneratorProcessor(MavenProject project, Log logger) {
        this.logger = logger;
        this.project = project;
    }

    public GeneratorProcessor input(@Nonnull String input) {
        this.input = Objects.requireNonNull(input);
        return this;
    }

    public GeneratorProcessor outputDir(@Nonnull String outputDir) {
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

    public void process() throws IOException, InterruptedException {
        var runner = new GeneratorShellRunner(project.getBasedir(), logger);
        prepareOutputDir(runner);
        preparePlugins(runner);
        prepareVerbose(runner);
        prepareInput(runner);
        runner.run();
    }

    public GeneratorProcessor verbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    private void prepareInput(GeneratorShellRunner runner) {
        runner.add("'" + GeneratorShellRunner
                .prepareJSONForCLI(Objects.requireNonNull(input)) + "'");
    }

    private void prepareOutputDir(GeneratorShellRunner runner) {
        var outputDirPath = Paths.get(outputDir);
        var result = outputDirPath.isAbsolute() ? outputDirPath
                : project.getBasedir().toPath().resolve(outputDir);
        runner.add("-o", result.toString());
    }

    private void preparePlugins(GeneratorShellRunner runner) {
        plugins.stream().map(GeneratorConfiguration.Plugin::getPath).distinct()
                .forEach(path -> runner.add("-p", path));
    }

    private void prepareVerbose(GeneratorShellRunner runner) {
        if (verbose) {
            runner.add("-v");
        }
    }
}
