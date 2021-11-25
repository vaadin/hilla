package com.vaadin.fusion.maven;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
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

final class ParserProcessor {
    private static final String defaultAnnotationName = "com.vaadin.fusion.Endpoint";
    private static final List<ParserConfiguration.Plugin> defaultPlugins = List
            .of(new ParserConfiguration.Plugin(BackbonePlugin.class.getName()));
    private final ParserConfig.Builder builder = new ParserConfig.Builder();
    private final Log logger;
    private final MavenProject project;

    public ParserProcessor(MavenProject project, Log logger) {
        this.project = project;
        this.logger = logger;
    }

    public String process() {
        try {
            logger.debug("Starting JVM Parser");

            var openAPI = new Parser(builder.finish()).execute();
            var openAPIJSONString = new OpenAPIPrinter().writeAsString(openAPI);

            logger.debug("OpenAPI (JSON): " + openAPIJSONString);

            return openAPIJSONString;
        } catch (IOException e) {
            throw new ParserException(
                    "Failed processing OpenAPI generated from parsed Java code",
                    e);
        }
    }

    public void useClassPath(@Nonnull ParserClassPathConfiguration classPath) {
        var value = classPath.getValue();
        var delimiter = classPath.getDelimiter();

        var userDefinedClassPathElements = List.of(value.split(delimiter));

        builder.classPath(classPath.isOverride() ? userDefinedClassPathElements
                : Stream.of(getDefaultMavenClassPathElementsStream(),
                        userDefinedClassPathElements.stream())
                        .flatMap(Function.identity()).distinct()
                        .collect(Collectors.toList()));
    }

    public void useClassPath() {
        builder.classPath(getDefaultMavenClassPathElementsStream()
                .collect(Collectors.toList()));
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

            if (!fileName.endsWith("yml") && !fileName.endsWith("yaml")
                    && !fileName.endsWith("json")) {
                throw new IOException("No OpenAPI base file found");
            }

            builder.openAPISource(Files.readString(path),
                    fileName.endsWith("json")
                            ? ParserConfig.OpenAPIFileType.JSON
                            : ParserConfig.OpenAPIFileType.YAML);
        } catch (IOException e) {
            throw new ParserException("Failed loading OpenAPI spec file", e);
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

    private Stream<String> getDefaultMavenClassPathElementsStream() {
        try {
            return Stream
                    .of(project.getCompileClasspathElements().stream(),
                            project.getRuntimeClasspathElements().stream())
                    .flatMap(Function.identity()).distinct();
        } catch (DependencyResolutionRequiredException e) {
            throw new ParserException("Failed collecting Maven class path", e);
        }
    }

    private Set<String> processPlugins(
            Stream<ParserConfiguration.Plugin> stream) {
        return stream.map(ParserConfiguration.Plugin::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
