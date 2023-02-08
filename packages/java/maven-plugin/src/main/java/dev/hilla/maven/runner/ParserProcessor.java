package dev.hilla.maven.runner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.hilla.parser.core.OpenAPIFileType;
import dev.hilla.parser.core.Parser;
import dev.hilla.parser.core.PluginManager;

import io.swagger.v3.oas.models.OpenAPI;

public final class ParserProcessor {
    private static final Logger logger = LoggerFactory
            .getLogger(ParserProcessor.class);
    private final Path baseDir;
    private final ParserConfiguration.PluginsProcessor pluginsProcessor = new ParserConfiguration.PluginsProcessor();
    private Set<String> classPath;
    private String endpointAnnotationName = "dev.hilla.Endpoint";
    private String endpointExposedAnnotationName = "dev.hilla.EndpointExposed";
    private String openAPIPath;

    public ParserProcessor(Path baseDir, Set<String> classPath) {
        this.baseDir = baseDir;
        this.classPath = classPath;
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

    public ParserProcessor endpointExposedAnnotation(
            @Nonnull String endpointExposedAnnotationName) {
        this.endpointExposedAnnotationName = Objects
                .requireNonNull(endpointExposedAnnotationName);
        return this;
    }

    public ParserProcessor openAPIBase(@Nonnull String openAPIPath) {
        this.openAPIPath = Objects.requireNonNull(openAPIPath);
        return this;
    }

    public ParserProcessor plugins(
            @Nonnull ParserConfiguration.Plugins plugins) {
        this.pluginsProcessor.setConfig(plugins);

        return this;
    }

    public OpenAPI process() {
        var parser = new Parser().classPath(classPath)
                .endpointAnnotation(endpointAnnotationName)
                .endpointExposedAnnotation(endpointExposedAnnotationName);

        preparePlugins(parser);
        prepareOpenAPIBase(parser);

        logger.debug("Starting JVM Parser");

        return parser.execute();
    }

    private void prepareOpenAPIBase(Parser parser) {
        if (openAPIPath == null) {
            return;
        }

        try {
            var path = baseDir.resolve(openAPIPath);
            var fileName = path.getFileName().toString();

            if (!fileName.endsWith("yml") && !fileName.endsWith("yaml")
                    && !fileName.endsWith("json")) {
                throw new IOException("No OpenAPI base file found");
            }

            parser.openAPISource(Files.readString(path),
                    fileName.endsWith("json") ? OpenAPIFileType.JSON
                            : OpenAPIFileType.YAML);
        } catch (IOException e) {
            throw new ParserException("Failed loading OpenAPI spec file", e);
        }
    }

    private void preparePlugins(Parser parser) {
        var loadedPlugins = pluginsProcessor.process().stream()
                .map((plugin) -> PluginManager.load(plugin.getName(),
                        plugin.getOrder(), plugin.getConfiguration()))
                .collect(Collectors.toList());

        parser.plugins(loadedPlugins);
    }
}
