package com.vaadin.fusion.maven;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.vaadin.fusion.parser.core.Parser;
import com.vaadin.fusion.parser.core.ParserConfig;
import com.vaadin.fusion.parser.core.PluginManager;
import com.vaadin.fusion.parser.utils.OpenAPIPrinter;

final class ParserProcessor {
    private final Log logger;
    private final MavenProject project;
    private Set<String> classPath;
    private String endpointAnnotationName = "com.vaadin.fusion.Endpoint";
    private String openAPIPath;
    private Collection<ParserConfiguration.Plugin> plugins = ParserConfiguration.PluginsProcessor.defaults;

    public ParserProcessor(MavenProject project, Log logger) {
        this.project = project;
        this.logger = logger;

        try {
            classPath = Stream
                    .of(project.getCompileClasspathElements(),
                            project.getRuntimeClasspathElements())
                    .flatMap(Collection::stream).collect(Collectors.toSet());
        } catch (DependencyResolutionRequiredException e) {
            throw new ParserException("Failed collecting Maven class path", e);
        }
    }

    public ParserProcessor classPath(
            @Nonnull ParserClassPathConfiguration classPath) {
        var value = Objects.requireNonNull(classPath).getValue();
        var delimiter = classPath.getDelimiter();

        var userDefinedClassPathElements = Set.of(value.split(delimiter));

        this.classPath = classPath.isOverride() ? userDefinedClassPathElements
                : Stream.of(this.classPath, userDefinedClassPathElements)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());

        return this;
    }

    public ParserProcessor endpointAnnotation(
            @Nonnull String endpointAnnotationName) {
        this.endpointAnnotationName = Objects
                .requireNonNull(endpointAnnotationName);
        return this;
    }

    public ParserProcessor openAPIBase(@Nonnull String openAPIPath) {
        this.openAPIPath = Objects.requireNonNull(openAPIPath);
        return this;
    }

    public ParserProcessor plugins(
            @Nonnull ParserConfiguration.Plugins plugins) {
        this.plugins = new ParserConfiguration.PluginsProcessor(plugins)
                .process();

        return this;
    }

    public String process() {
        var builder = new ParserConfig.Builder().classPath(classPath)
                .endpointAnnotation(endpointAnnotationName);

        preparePlugins(builder);
        prepareOpenAPIBase(builder);

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

    private void prepareOpenAPIBase(ParserConfig.Builder builder) {
        if (openAPIPath == null) {
            return;
        }

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

    private void preparePlugins(ParserConfig.Builder builder) {
        // For loaded plugins we use SortedSet to have them sorted in order the
        // user defined.
        var loadedPlugins = plugins.stream()
                .map((plugin) -> PluginManager.load(plugin.getName(),
                        plugin.getOrder(), plugin.getConfiguration()))
                .collect(Collectors.toCollection(TreeSet::new));

        builder.plugins(loadedPlugins);
    }
}
