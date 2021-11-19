package com.vaadin.fusion.maven;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public class GeneratorExecutor {
    private static final List<GeneratorConfiguration.Plugin> defaultPlugins = List
            .of(new GeneratorConfiguration.Plugin(
                    "generator-typescript-plugin-backbone"));
    private static final String defaultOutputDir = "frontend/generated";

    private final ShellProcess process = new ShellProcess();
    private final MavenProject project;
    private final Log logger;

    public GeneratorExecutor(MavenProject project, Log logger) {
        this.logger = logger;
        this.project = project;

        process.command("npx", "tsgen");
    }

    public void execute() {
        logger.info(String.format("Running command: %s",
                String.join(" ", process.getCommand())));

        try {
            process.start();
        } catch (IOException e) {
            throw new GeneratorMavenPluginException(
                    "Failed running TS Generator", e);
        }
    }

    public void useInput(@Nonnull String input) {
        process.command("'" + input + "'");
    }

    public void useOutputDir() {
        var outputDirPath = Paths.get(project.getBasedir().getAbsolutePath(),
                defaultOutputDir);
        process.command("-o", outputDirPath.toString());
    }

    public void useOutputDir(@Nonnull String outputDir) {
        var userDefinedOutputDirPath = Paths.get(outputDir);
        var outputDirPath = userDefinedOutputDirPath.isAbsolute()
                ? userDefinedOutputDirPath
                : Paths.get(project.getBasedir().getAbsolutePath(), outputDir);
        process.command("-o", outputDirPath.toString());
    }

    public void usePlugins(@Nonnull GeneratorConfiguration.PluginList plugins) {
        var pluginStream = plugins.getUse().stream();

        if (!plugins.isDisableAllDefaults()) {
            pluginStream = Stream.concat(
                    defaultPlugins.stream().filter(
                            plugin -> !plugins.getDisable().contains(plugin)),
                    pluginStream);
        }

        process.command(processPlugins(pluginStream));
    }

    public void usePlugins() {
        process.command(processPlugins(defaultPlugins.stream()));
    }

    private List<String> processPlugins(Stream<GeneratorConfiguration.Plugin> stream) {
        return stream.map(GeneratorConfiguration.Plugin::getName)
                .map(name -> Paths.get(project.getBasedir().getAbsolutePath(),
                        "node_modules", name, "index.js").toString())
                .distinct().flatMap(path -> Stream.of("-p", path))
                .collect(Collectors.toList());
    }
}
