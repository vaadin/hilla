package dev.hilla.engine;

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
    private final ClassLoader classLoader;
    private Set<String> classPath;
    private String endpointAnnotationName = "dev.hilla.Endpoint";
    private String endpointExposedAnnotationName = "dev.hilla.EndpointExposed";
    private String openAPIBase;

    public ParserProcessor(Path baseDir, ClassLoader classLoader,
        Set<String> classPath) {
        this.baseDir = baseDir;
        this.classLoader = classLoader;
        this.classPath = classPath;
    }

    public ParserProcessor apply(ParserConfiguration parserConfiguration) {
        if (parserConfiguration == null) {
            return this;
        }

        parserConfiguration.getClassPath().ifPresent(this::applyClassPath);
        parserConfiguration.getEndpointAnnotation()
            .ifPresent(this::applyEndpointAnnotation);
        parserConfiguration.getEndpointExposedAnnotation()
            .ifPresent(this::applyEndpointExposedAnnotation);
        parserConfiguration.getOpenAPIPath().ifPresent(this::applyOpenAPIBase);
        parserConfiguration.getPlugins().ifPresent(this::applyPlugins);
        return this;
    }

    public OpenAPI process() {
        var parser = new Parser().classLoader(classLoader).classPath(classPath)
            .endpointAnnotation(endpointAnnotationName)
            .endpointExposedAnnotation(endpointExposedAnnotationName);

        preparePlugins(parser);
        prepareOpenAPIBase(parser);

        logger.debug("Starting JVM Parser");

        return parser.execute();
    }

    private void applyClassPath(
        @Nonnull ParserClassPathConfiguration classPath) {
        var value = Objects.requireNonNull(classPath).getValue();
        var delimiter = classPath.getDelimiter();

        var userDefinedClassPathElements = Set.of(value.split(delimiter));

        this.classPath = classPath.isOverride() ? userDefinedClassPathElements
                : Stream.of(this.classPath, userDefinedClassPathElements)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());
    }

    private void applyEndpointAnnotation(
            @Nonnull String endpointAnnotationName) {
        this.endpointAnnotationName = Objects
                .requireNonNull(endpointAnnotationName);
    }

    private void applyEndpointExposedAnnotation(
            @Nonnull String endpointExposedAnnotationName) {
        this.endpointExposedAnnotationName = Objects
                .requireNonNull(endpointExposedAnnotationName);
    }

    private void applyOpenAPIBase(@Nonnull String openAPIBase) {
        this.openAPIBase = openAPIBase;
    }

    private void applyPlugins(
        @Nonnull ParserConfiguration.Plugins plugins) {
        this.pluginsProcessor.setConfig(plugins);
    }

    private void prepareOpenAPIBase(Parser parser) {
        if (openAPIBase == null) {
            return;
        }

        try {
            var path = baseDir.resolve(openAPIBase);
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
