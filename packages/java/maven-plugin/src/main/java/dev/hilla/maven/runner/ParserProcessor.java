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

import dev.hilla.parser.core.Parser;
import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.core.PluginManager;

import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ParserProcessor {
    private final Path baseDir;
    private static final Logger logger = LoggerFactory
            .getLogger(ParserProcessor.class);
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
        var builder = new ParserConfig.Builder().classPath(classPath)
                .endpointAnnotation(endpointAnnotationName)
                .endpointExposedAnnotation(endpointExposedAnnotationName);

        preparePlugins(builder);
        prepareOpenAPIBase(builder);

        logger.debug("Starting JVM Parser");

        return new Parser(builder.finish()).execute();
    }

    private void prepareOpenAPIBase(ParserConfig.Builder builder) {
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

            builder.openAPISource(Files.readString(path),
                    fileName.endsWith("json")
                            ? ParserConfig.OpenAPIFileType.JSON
                            : ParserConfig.OpenAPIFileType.YAML);
        } catch (IOException e) {
            throw new ParserException("Failed loading OpenAPI spec file", e);
        }
    }

    private void preparePlugins(ParserConfig.Builder builder) {
        var loadedPlugins = pluginsProcessor.process().stream()
                .map((plugin) -> PluginManager.load(plugin.getName(),
                        plugin.getOrder(), plugin.getConfiguration()))
                .collect(Collectors.toList());

        builder.plugins(loadedPlugins);
    }
}
