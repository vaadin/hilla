package com.vaadin.fusion.maven;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

final class GeneratorProcessor {
    private static final Pattern jsonEscapePattern = Pattern.compile("[\r\n\b\f\t\"']");
    private static final String defaultOutputDir = "frontend/generated";
    private static final List<GeneratorConfiguration.Plugin> defaultPlugins = List
            .of(new GeneratorConfiguration.Plugin(
                    "generator-typescript-plugin-backbone"));
    private final GeneratorShellRunner runner;
    private final MavenProject project;

    public GeneratorProcessor(MavenProject project, Log logger) {
        runner = new GeneratorShellRunner(List.of("npx", "tsgen"), logger);
        this.project = project;
    }

    public void process() throws IOException, InterruptedException {
        runner.run();
    }

    public void useInput(@Nonnull String input) {
        var result = jsonEscapePattern.matcher(input).replaceAll(match -> "\\\\" + match.group(0));
        runner.add("'" + result + "'");
    }

    public void useVerbose() {
        useVerbose(false);
    }

    public void useVerbose(boolean verbose) {
        if (verbose) {
            runner.add("-v");
        }
    }

    public void useOutputDir() {
        useOutputDir(defaultOutputDir);
    }

    public void useOutputDir(@Nonnull String outputDir) {
        var userDefinedOutputDirPath = Paths.get(outputDir);
        var outputDirPath = userDefinedOutputDirPath.isAbsolute()
                ? userDefinedOutputDirPath
                : Paths.get(project.getBasedir().getAbsolutePath(), outputDir);
        runner.add("-o", outputDirPath.toString());
    }

    public void usePlugins(@Nonnull GeneratorConfiguration.PluginList plugins) {
        var pluginStream = plugins.getUse().stream();

        if (!plugins.isDisableAllDefaults()) {
            pluginStream = Stream.concat(
                    defaultPlugins.stream().filter(
                            plugin -> !plugins.getDisable().contains(plugin)),
                    pluginStream);
        }

        runner.add(processPlugins(pluginStream));
    }

    public void usePlugins() {
        runner.add(processPlugins(defaultPlugins.stream()));
    }

    private String[] processPlugins(
            Stream<GeneratorConfiguration.Plugin> stream) {
        return stream.map(plugin -> plugin.resolveWithin(project).toString())
                .distinct().flatMap(path -> Stream.of("-p", path))
                .toArray(String[]::new);
    }

}
