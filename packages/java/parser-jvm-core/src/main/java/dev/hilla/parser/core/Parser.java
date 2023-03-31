package dev.hilla.parser.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.classgraph.ClassGraph;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * The entrypoint class. It searches for the endpoint classes in the classpath
 * and produces an OpenAPI definition.
 */
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

    private static OpenAPI parseOpenAPIFile(@Nonnull String source,
            @Nonnull OpenAPIFileType type, OpenAPI origin) {
        try {
            var mapper = type.getMapper();
            var reader = origin != null ? mapper.readerForUpdating(origin)
                    : mapper.reader();
            return reader.readValue(source, OpenAPI.class);
        } catch (IOException e) {
            throw new ParserException("Failed to parse openAPI specification",
                    e);
        }
    }

    /**
     * Adds a parser {@link Plugin}.
     *
     * <p>
     * Note that the order of the method calls will be maintained during
     * processing.
     *
     * @param plugin
     *            An instance of the parser plugin.
     * @return this (for method chaining).
     */
    @Nonnull
    public Parser addPlugin(@Nonnull Plugin plugin) {
        config.plugins.add(Objects.requireNonNull(plugin));
        return this;
    }

    /**
     * Allows to programmatically change the default OpenAPI definition.
     *
     * @param action
     *            a consumer lambda that accepts an OpenAPI instance.
     * @return this (for method chaining).
     */
    @Nonnull
    public Parser adjustOpenAPI(@Nonnull Consumer<OpenAPI> action) {
        action.accept(config.openAPI);
        return this;
    }

    /**
     * Allows to change the class loader that the parser uses for reflection.
     *
     * @param classLoader
     *            a class loader instance.
     *
     * @return this (for method chaining).
     */
    @Nonnull
    public Parser classLoader(@Nonnull ClassLoader classLoader) {
        config.classLoader = classLoader;
        return this;
    }

    /**
     * Specifies the classpath where the parser will scan for endpoints.
     * Specifying the classpath is required.
     *
     * <p>
     * If the classpath is already set, it will be overridden.
     *
     * @param classPathElements
     *            a list of paths forming the classpath.
     * @return this (for method chaining).
     */
    @Nonnull
    public Parser classPath(@Nonnull String... classPathElements) {
        return classPath(classPathElements, true);
    }

    /**
     * Specifies the classpath where the parser will scan for endpoints.
     * Specifying the classpath is required.
     *
     * @param classPathElements
     *            a list of paths forming the classpath.
     * @param override
     *            specifies if the parser should override the classpath if it is
     *            already specified.
     * @return this (for method chaining).
     */
    @Nonnull
    public Parser classPath(@Nonnull String[] classPathElements,
            boolean override) {
        return classPath(
                Arrays.asList(Objects.requireNonNull(classPathElements)),
                override);
    }

    /**
     * Specifies the classpath where the parser will scan for endpoints.
     * Specifying the classpath is required.
     *
     * <p>
     * If the classpath is already set, it will be overridden.
     *
     * @param classPathElements
     *            a list of paths forming the classpath.
     * @return this (for method chaining).
     */
    @Nonnull
    public Parser classPath(@Nonnull Collection<String> classPathElements) {
        return classPath(classPathElements, true);
    }

    /**
     * Specifies the classpath where the parser will scan for endpoints.
     * Specifying the classpath is required.
     *
     * @param classPathElements
     *            a list of paths forming the classpath.
     * @param override
     *            specifies if the parser should override the classpath if it is
     *            already specified.
     * @return this (for method chaining).
     */
    @Nonnull
    public Parser classPath(@Nonnull Collection<String> classPathElements,
            boolean override) {
        if (override || config.classPathElements == null) {
            config.classPathElements = new HashSet<>(
                    Objects.requireNonNull(classPathElements));
        }
        return this;
    }

    /**
     * Specifies the name of the endpoint annotation by which the parser will
     * search for the endpoints. Only classes with this annotation will be
     * chosen.
     *
     * <p>
     * If the annotation name is already set, it will be overridden.
     *
     * @param annotationFullyQualifiedName
     *            The fully qualified name of the annotation
     * @return this (for method chaining).
     */
    @Nonnull
    public Parser endpointAnnotation(
            @Nonnull String annotationFullyQualifiedName) {
        return endpointAnnotation(annotationFullyQualifiedName, true);
    }

    /**
     * Specifies the name of the endpoint annotation by which the parser will
     * search for the endpoints. Only classes with this annotation will be
     * chosen.
     *
     * @param annotationFullyQualifiedName
     *            The fully qualified name of the annotation
     * @param override
     *            specifies if the parser should override the annotation name if
     *            it is already specified.
     * @return this (for method chaining).
     */
    @Nonnull
    public Parser endpointAnnotation(
            @Nonnull String annotationFullyQualifiedName, boolean override) {
        if (override || config.endpointAnnotationName == null) {
            config.endpointAnnotationName = Objects
                    .requireNonNull(annotationFullyQualifiedName);
        }
        return this;
    }

    /**
     * Specifies the name of the `EndpointExposed` annotation by which the
     * parser will detect if the endpoint superclass should be considered as the
     * part of the endpoint. Any superclass in the endpoint's inheritance chain
     * will be skipped if it doesn't have this annotation.
     *
     * <p>
     * If the annotation name is already set, it will be overridden.
     *
     * @param annotationFullyQualifiedName
     *            The fully qualified name of the annotation
     * @return this (for method chaining).
     */
    @Nonnull
    public Parser endpointExposedAnnotation(
            @Nonnull String annotationFullyQualifiedName) {
        return endpointExposedAnnotation(annotationFullyQualifiedName, true);
    }

    /**
     * Specifies the name of the `EndpointExposed` annotation by which the
     * parser will detect if the endpoint superclass should be considered as the
     * part of the endpoint. Any superclass in the endpoint's inheritance chain
     * will be skipped if it doesn't have this annotation.
     *
     * @param annotationFullyQualifiedName
     *            The fully qualified name of the annotation
     * @param override
     *            specifies if the parser should override the annotation name if
     *            it is already specified.
     * @return this (for method chaining).
     */
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
    public Parser exposedPackages(@Nonnull Collection<String> exposedPackages) {
        return exposedPackages(exposedPackages, true);
    }

    @Nonnull
    public Parser exposedPackages(@Nonnull Collection<String> exposedPackages,
            boolean override) {
        if (override || config.exposedPackages == null) {
            config.exposedPackages = Objects.requireNonNull(exposedPackages);
        }
        return this;
    }

    /**
     * Scans the classpath, blocking until the scan is complete.
     *
     * @return A result OpenAPI object.
     */
    @Nonnull
    public OpenAPI execute() {
        Objects.requireNonNull(config.classLoader,
                "[JVM Parser] classLoader is not provided.");
        Objects.requireNonNull(config.classPathElements,
                "[JVM Parser] classPath is not provided.");
        Objects.requireNonNull(config.endpointAnnotationName,
                "[JVM Parser] endpointAnnotationName is not provided.");

        logger.info("JVM Parser started");

        var storage = new SharedStorage(config);

        var classGraph = new ClassGraph().enableAnnotationInfo()
                .ignoreClassVisibility()
                .overrideClassLoaders(config.getClassLoader());

        Collection<String> packages = config.exposedPackages;

        // Packages explicitly defined in pom.xml have priority
        if (packages != null && !packages.isEmpty()) {
            logger.info("Search for endpoints in packages {}", packages);
            classGraph.acceptPackages(packages.toArray(String[]::new));
            classGraph.overrideClasspath(config.getClassPathElements());
        }
        // If no packages are defined, then scan the whole classpath except
        // jars, which basically means scanning the build or target folder
        else {
            var buildDirectories = config.getClassPathElements().stream()
                    .filter(e -> !e.endsWith(".jar"))
                    .collect(Collectors.toList());
            logger.info("Search for endpoints in directories {}",
                    buildDirectories);
            classGraph.overrideClasspath(buildDirectories);
        }

        try (var scanResult = classGraph.scan()) {
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

    /**
     * Gets the internal configuration object.
     *
     * @return configuration object.
     */
    @Nonnull
    public Config getConfig() {
        return config;
    }

    /**
     * Parses the OpenAPI source string with the provided parser and merges the
     * result into the current OpenAPI object. This method is useful if you want
     * to adjust some basic parts of the OpenAPI object like the application
     * title, version, server description or URL.
     *
     * <p>
     * If the method is used once, all the changes will be applied to the
     * default OpenAPI object. Called multiple time, this function applies
     * changes one by one in the order of method calls.
     *
     * @param source
     *            The OpenAPI definition in the JSON or YAML format. You don't
     *            have to specify all the fields required by the schema; the
     *            result definition will use default fields.
     * @param type
     *            The parser for the OpenAPI definition
     * @return this (for method chaining).
     */
    @Nonnull
    public Parser openAPISource(@Nonnull String source,
            @Nonnull OpenAPIFileType type) {
        config.openAPI = parseOpenAPIFile(Objects.requireNonNull(source),
                Objects.requireNonNull(type), config.openAPI);

        return this;
    }

    /**
     * Adds a collection of parser {@link Plugin}s. If there are plugins already
     * specified, they will be removed before addition.
     *
     * <p>
     * Note that the order of the arguments will be maintained during
     * processing.
     *
     * @param plugins
     *            a collection of parser plugins.
     * @return this (for method chaining).
     */
    @Nonnull
    public Parser plugins(@Nonnull Plugin... plugins) {
        return plugins(Arrays.asList(plugins));
    }

    /**
     * Adds a collection of parser {@link Plugin}s. If there are already
     * specified plugins, they will be removed before addition.
     *
     * <p>
     * Note that the order of collection will be maintained during processing.
     *
     * @param plugins
     *            a collection of parser plugins.
     * @return this (for method chaining).
     */
    @Nonnull
    public Parser plugins(@Nonnull Collection<? extends Plugin> plugins) {
        config.plugins.clear();
        config.plugins.addAll(Objects.requireNonNull(plugins));
        return this;
    }

    /**
     * An immutable parser configuration object. It allows to peek into the
     * initial configuration of the parser during the scan.
     */
    public static final class Config {
        private final List<Plugin> plugins = new ArrayList<>();
        private Set<String> classPathElements;
        private String endpointAnnotationName;
        private String endpointExposedAnnotationName;
        private Collection<String> exposedPackages;
        private OpenAPI openAPI;
        private ClassLoader classLoader;

        private Config(OpenAPI openAPI) {
            this.openAPI = openAPI;
        }

        /**
         * Gets the class loader for reflection in the parser.
         *
         * @return the class loader
         */
        @Nonnull
        public ClassLoader getClassLoader() {
            return classLoader;
        }

        /**
         * Gets the collection of classpath elements.
         *
         * @return the collection of classpath elements.
         */
        @Nonnull
        public Set<String> getClassPathElements() {
            return classPathElements;
        }

        /**
         * Gets the name of endpoint annotation.
         *
         * @return the annotation name.
         */
        @Nonnull
        public String getEndpointAnnotationName() {
            return endpointAnnotationName;
        }

        /**
         * Gets the name of `EndpointExposed` annotation.
         *
         * @return the annotation name.
         */
        @Nonnull
        public String getEndpointExposedAnnotationName() {
            return endpointExposedAnnotationName;
        }

        @Nonnull
        public Collection<String> getExposedPackages() {
            return exposedPackages;
        }

        /**
         * Gets the OpenAPI object.
         *
         * <p>
         * Note that the object is mutable.
         *
         * @return OpenAPI object.
         */
        @Nonnull
        public OpenAPI getOpenAPI() {
            return openAPI;
        }

        /**
         * Returns a collection of parser plugins.
         *
         * @return the collection of parser plugins.
         */
        @Nonnull
        public Collection<Plugin> getPlugins() {
            return plugins;
        }

    }
}
