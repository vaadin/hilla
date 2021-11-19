package com.vaadin.fusion.maven;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.maven.project.MavenProject;

public class GeneratorExecutor {
    private static final List<GeneratorConfiguration.Plugin> defaultPlugins = List
            .of(new GeneratorConfiguration.Plugin(
                    "generator-typescript-plugin-backbone"));
    private static final String defaultOutputDir = "frontend/generated";

    private final ProcessBuilder builder = new ProcessBuilder();
    private final MavenProject project;

    public GeneratorExecutor(MavenProject project) {
        this.project = project;
        builder.directory(project.getBasedir()).command("npx", "tsgen");
    }

    public void execute() {
        try {
            builder.start();
        } catch (IOException e) {
            throw new FusionMavenPluginException("Error running Fusion TS Generator", e);
        }
    }

    public void useInput(@Nonnull String input) {
        builder.command(input);
    }

    public void useOutputDir() {
        var outputDirPath = Paths.get(project.getBasedir().getAbsolutePath(),
                defaultOutputDir);
        builder.command("-o", outputDirPath.toString());
    }

    public void useOutputDir(@Nonnull String outputDir) {
        var userDefinedOutputDirPath = Paths.get(outputDir);
        var outputDirPath = userDefinedOutputDirPath.isAbsolute()
                ? userDefinedOutputDirPath
                : Paths.get(project.getBasedir().getAbsolutePath(), outputDir);
        builder.command("-o", outputDirPath.toString());
    }

    public void usePlugins(@Nonnull GeneratorConfiguration.PluginList plugins) {
        var pluginStream = plugins.getUse().stream();

        if (!plugins.isDisableAllDefaults()) {
            pluginStream = Stream.concat(
                    defaultPlugins.stream().filter(
                            plugin -> !plugins.getDisable().contains(plugin)),
                    pluginStream);
        }

        builder.command(processPlugins(pluginStream));
    }

    public void usePlugins() {
        builder.command(processPlugins(defaultPlugins.stream()));
    }

    private List<String> processPlugins(
            Stream<GeneratorConfiguration.Plugin> stream) {
        return stream.map(GeneratorConfiguration.Plugin::getName)
                .map(name -> Paths.get(project.getBasedir().getAbsolutePath(),
                        "node_modules", name, "index.js").toString())
                .distinct().flatMap(path -> Stream.of("-p", path))
                .collect(Collectors.toList());
    }
}
