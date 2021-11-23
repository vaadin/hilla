package com.vaadin.fusion.maven.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.vaadin.fusion.parser.core.OpenAPIPrinter;
import com.vaadin.fusion.parser.core.Parser;
import com.vaadin.fusion.parser.core.ParserConfig;
import com.vaadin.fusion.parser.plugins.backbone.BackbonePlugin;

public class ParserProcessor {
    private static final List<ParserConfiguration.Plugin> defaultPlugins = List
            .of(new ParserConfiguration.Plugin(BackbonePlugin.class.getName()));
    private static final String defaultAnnotationName = "com.vaadin.fusion.Endpoint";

    private final ParserConfig.Builder builder = new ParserConfig.Builder();
    private final MavenProject project;
    private final Log logger;

    public ParserProcessor(MavenProject project, Log logger) {
        this.project = project;
        this.logger = logger;
    }

    public String execute() {
        try {
            var api = new Parser(builder.finish()).execute();
            return new OpenAPIPrinter().writeAsString(api);
        } catch (IOException e) {
            throw new ParserException(
                    "Failed processing OpenAPI generated from parsed Java code",
                    e);
        }
    }

    public void useClassPath(@Nonnull ParserClassPathConfiguration classPath) {
        try {
            var result = classPath.isOverride() ? classPath.getValue()
                    : Stream.concat(
                            project.getCompileClasspathElements().stream(),
                            Stream.of(classPath.getValue()))
                            .collect(Collectors.joining(";"));

            builder.classPath(result);
        } catch (DependencyResolutionRequiredException e) {
            throw new ParserException(
                    "Failed collecting class path", e);
        }
    }

    public void useClassPath() {
        try {
            builder.classPath(
                    String.join(";", project.getCompileClasspathElements()));
        } catch (DependencyResolutionRequiredException e) {
            throw new ParserException(
                    "Failed collecting class path", e);
        }
    }

    public void useEndpointAnnotation() {
        builder.endpointAnnotation(defaultAnnotationName);
    }

    public void useEndpointAnnotation(@Nonnull String endpointAnnotation) {
        builder.endpointAnnotation(endpointAnnotation);
    }

    public void useOpenAPIBase(@Nonnull String openAPIPath) {
        try {
            var path = Paths.get(project.getBasedir().getAbsolutePath(),
                    openAPIPath);
            var fileName = path.getFileName().toString();
            var dotLastIndex = fileName.lastIndexOf('.');

            if (dotLastIndex == -1) {
                throw new IOException("No OpenAPI base file found");
            }

            // dotLastIndex + 1 because we want to get extension without the dot.
            builder.openAPISpec(Files.readString(path), fileName.substring(dotLastIndex + 1));
        } catch (IOException e) {
            throw new ParserException(
                    "Failed loading OpenAPI spec file", e);
        }
    }

    public void usePlugins(@Nonnull ParserConfiguration.PluginList plugins) {
        var pluginStream = plugins.getUse().stream();

        if (!plugins.isDisableAllDefaults()) {
            pluginStream = Stream.concat(
                    defaultPlugins.stream().filter(
                            plugin -> !plugins.getDisable().contains(plugin)),
                    pluginStream);
        }

        builder.plugins(processPlugins(pluginStream));
    }

    public void usePlugins() {
        builder.plugins(processPlugins(defaultPlugins.stream()));
    }

    private Set<String> processPlugins(
            Stream<ParserConfiguration.Plugin> stream) {
        return stream.map(ParserConfiguration.Plugin::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
