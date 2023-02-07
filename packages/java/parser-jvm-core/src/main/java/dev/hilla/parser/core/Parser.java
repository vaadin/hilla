package dev.hilla.parser.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.classgraph.ClassGraph;
import io.swagger.v3.oas.models.OpenAPI;

public final class Parser {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);
    private final Config config;

    public Parser() {
        try {
            var basicOpenAPIString = new String(Objects
                    .requireNonNull(Parser.class
                            .getResourceAsStream("OpenAPIBase.json"))
                    .readAllBytes());
            var openAPI = parseOpenAPIFile(basicOpenAPIString,
                    OpenAPIFileType.JSON, null);
            this.config = new Config(openAPI);
        } catch (IOException e) {
            throw new ParserException("Failed to parse openAPI specification",
                    e);
        }
    }

    private static OpenAPI parseOpenAPIFile(@Nonnull String contents,
            @Nonnull OpenAPIFileType type, OpenAPI origin) {
        try {
            var mapper = type.getMapper();
            var reader = origin != null ? mapper.readerForUpdating(origin)
                    : mapper.reader();
            return reader.readValue(contents, OpenAPI.class);
        } catch (IOException e) {
            throw new ParserException("Failed to parse openAPI specification",
                    e);
        }
    }

    @Nonnull
    public Parser addPlugin(@Nonnull Plugin plugin) {
        config.plugins.add(Objects.requireNonNull(plugin));
        return this;
    }

    @Nonnull
    public Parser adjustOpenAPI(@Nonnull Consumer<OpenAPI> action) {
        action.accept(config.openAPI);
        return this;
    }

    @Nonnull
    public Parser classPath(@Nonnull String[] classPathElements) {
        return classPath(classPathElements, true);
    }

    @Nonnull
    public Parser classPath(@Nonnull String[] classPathElements,
            boolean override) {
        return classPath(
                Arrays.asList(Objects.requireNonNull(classPathElements)),
                override);
    }

    @Nonnull
    public Parser classPath(@Nonnull Collection<String> classPathElements,
            boolean override) {
        if (override || config.classPathElements == null) {
            config.classPathElements = new HashSet<>(
                    Objects.requireNonNull(classPathElements));
        }
        return this;
    }

    @Nonnull
    public Parser classPath(@Nonnull Collection<String> classPathElements) {
        return classPath(classPathElements, true);
    }

    @Nonnull
    public Parser endpointAnnotation(
            @Nonnull String annotationFullyQualifiedName, boolean override) {
        if (override || config.endpointAnnotationName == null) {
            config.endpointAnnotationName = Objects
                    .requireNonNull(annotationFullyQualifiedName);
        }
        return this;
    }

    @Nonnull
    public Parser endpointAnnotation(@Nonnull String annotationQualifiedName) {
        return endpointAnnotation(annotationQualifiedName, true);
    }

    @Nonnull
    public Parser endpointExposedAnnotation(
            @Nonnull String annotationFullyQualifiedName, boolean override) {
        if (override || config.endpointExposedAnnotationName == null) {
            config.endpointExposedAnnotationName = Objects
                    .requireNonNull(annotationFullyQualifiedName);
        }
        return this;
    }

    @Nonnull
    public Parser endpointExposedAnnotation(
            @Nonnull String annotationQualifiedName) {
        return endpointExposedAnnotation(annotationQualifiedName, true);
    }

    public OpenAPI execute() {
        Objects.requireNonNull(config.classPathElements,
                "[JVM Parser] classPath is not provided.");
        Objects.requireNonNull(config.endpointAnnotationName,
                "[JVM Parser] endpointAnnotationName is not provided.");

        logger.info("JVM Parser started");

        var storage = new SharedStorage(config);

        try (var scanResult = new ClassGraph().enableAnnotationInfo()
                .ignoreClassVisibility()
                .overrideClasspath(config.getClassPathElements()).scan()) {
            var rootNode = new RootNode(new ScanResult(scanResult),
                    storage.getOpenAPI());
            var pluginManager = new PluginManager(
                    storage.getParserConfig().getPlugins());
            pluginManager.setStorage(storage);
            var pluginExecutor = new PluginExecutor(pluginManager, rootNode);
            pluginExecutor.execute();
        }

        logger.info("JVM Parser finished successfully");

        return storage.getOpenAPI();
    }

    public Config getConfig() {
        return config;
    }

    @Nonnull
    public Parser openAPISource(@Nonnull String path,
            @Nonnull OpenAPIFileType type) {
        config.openAPI = parseOpenAPIFile(Objects.requireNonNull(path),
                Objects.requireNonNull(type), config.openAPI);

        return this;
    }

    public Parser plugins(Plugin... plugins) {
        return plugins(Arrays.asList(plugins));
    }

    @Nonnull
    public Parser plugins(@Nonnull Collection<? extends Plugin> plugins) {
        config.plugins.clear();
        config.plugins.addAll(Objects.requireNonNull(plugins));
        return this;
    }

    public static final class Config {
        private final SortedSet<Plugin> plugins = new TreeSet<>(
                Comparator.comparingInt(Plugin::getOrder));
        private Set<String> classPathElements;
        private String endpointAnnotationName;
        private String endpointExposedAnnotationName;
        private OpenAPI openAPI;

        private Config(OpenAPI openAPI) {
            this.openAPI = openAPI;
        }

        @Nonnull
        public Set<String> getClassPathElements() {
            return classPathElements;
        }

        @Nonnull
        public String getEndpointAnnotationName() {
            return endpointAnnotationName;
        }

        @Nonnull
        public String getEndpointExposedAnnotationName() {
            return endpointExposedAnnotationName;
        }

        @Nonnull
        public OpenAPI getOpenAPI() {
            return openAPI;
        }

        @Nonnull
        public SortedSet<Plugin> getPlugins() {
            return plugins;
        }
    }
}
