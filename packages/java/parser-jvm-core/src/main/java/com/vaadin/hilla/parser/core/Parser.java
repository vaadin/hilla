package com.vaadin.hilla.parser.core;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * The entrypoint class. It searches for the endpoint classes in the classpath
 * and produces an OpenAPI definition.
 */
public final class Parser {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);
    private final Config config;

    private static final String ENDPOINT_EXPOSED_AND_ACL_ANNOTATIONS_ERROR_TEMPLATE = """
            Class `%s` is annotated with `%s` and `%s` annotation. %n
            Classes annotated with `%s` must not contain any of access control annotations and %n
            this exception is for preventing the application startup with misconfiguration. The class level access %n
            control rules of the child class will be applied for the inherited methods of this class. If the access %n
            control rules for an inherited method should not follow the rules of the child endpoint, that method %n
            should be overridden and annotated with the desired access control annotations explicitly. %n
            """
            .stripIndent();

    private static final Set<String> ACL_ANNOTATIONS = Set.of(
            "jakarta.annotation.security.DenyAll",
            "jakarta.annotation.security.PermitAll",
            "jakarta.annotation.security.RolesAllowed",
            "com.vaadin.flow.server.auth.AnonymousAllowed");

    private static final List<String> INTERNAL_BROWSER_CALLABLES = List
            .of("com.vaadin.hilla.signals.handler.SignalsHandler");

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

    private static OpenAPI parseOpenAPIFile(@NonNull String source,
            @NonNull OpenAPIFileType type, OpenAPI origin) {
        try {
            var mapper = type.getMapper();
            var reader = origin != null ? mapper.readerForUpdating(origin)
                    : mapper.readerFor(OpenAPI.class);
            return reader.readValue(source);
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
    @NonNull
    public Parser addPlugin(@NonNull Plugin plugin) {
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
    @NonNull
    public Parser adjustOpenAPI(@NonNull Consumer<OpenAPI> action) {
        action.accept(config.openAPI);
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
    @NonNull
    public Parser classPath(@NonNull String... classPathElements) {
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
    @NonNull
    public Parser classPath(@NonNull String[] classPathElements,
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
    @NonNull
    public Parser classPath(@NonNull Collection<String> classPathElements) {
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
    @NonNull
    public Parser classPath(@NonNull Collection<String> classPathElements,
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
     * @param annotations
     *            The fully qualified names of the annotations
     * @return this (for method chaining).
     */
    @NonNull
    public Parser endpointAnnotations(
            @NonNull List<Class<? extends Annotation>> annotations) {
        return endpointAnnotations(annotations, true);
    }

    /**
     * Specifies the name of the endpoint annotation by which the parser will
     * search for the endpoints. Only classes with this annotation will be
     * chosen.
     *
     * @param annotations
     *            The fully qualified names of the annotations
     * @param override
     *            specifies if the parser should override the annotation name if
     *            it is already specified.
     * @return this (for method chaining).
     */
    @NonNull
    public Parser endpointAnnotations(
            @NonNull List<Class<? extends Annotation>> annotations,
            boolean override) {
        if (override || config.endpointAnnotations == null) {
            config.endpointAnnotations = Objects.requireNonNull(annotations);
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
     * @param annotations
     *            The fully qualified names of the annotations
     * @return this (for method chaining).
     */
    @NonNull
    public Parser endpointExposedAnnotations(
            @NonNull List<Class<? extends Annotation>> annotations) {
        return endpointExposedAnnotations(annotations, true);
    }

    /**
     * Specifies the name of the `EndpointExposed` annotation by which the
     * parser will detect if the endpoint superclass should be considered as the
     * part of the endpoint. Any superclass in the endpoint's inheritance chain
     * will be skipped if it doesn't have this annotation.
     *
     * @param annotations
     *            The fully qualified names of the annotations
     * @param override
     *            specifies if the parser should override the annotation name if
     *            it is already specified.
     * @return this (for method chaining).
     */
    @NonNull
    public Parser endpointExposedAnnotations(
            @NonNull List<Class<? extends Annotation>> annotations,
            boolean override) {
        if (override || config.endpointExposedAnnotations == null) {
            config.endpointExposedAnnotations = Objects
                    .requireNonNull(annotations);
        }
        return this;
    }

    /**
     * Scans the classpath, blocking until the scan is complete.
     *
     * @return A result OpenAPI object.
     */
    @NonNull
    public OpenAPI execute(List<Class<?>> browserCallables) {
        Objects.requireNonNull(config.classPathElements,
                "[JVM Parser] classPath is not provided.");
        if (config.endpointAnnotations == null
                || config.endpointAnnotations.isEmpty()) {
            throw new IllegalArgumentException(
                    "[JVM Parser] endpoint annotations are not provided.");
        }

        logger.debug("JVM Parser started");

        browserCallables = browserCallables.stream().filter(
                cls -> !INTERNAL_BROWSER_CALLABLES.contains(cls.getName()))
                .toList();

        var storage = new SharedStorage(config);

        validateEndpointExposedClassesForAclAnnotations(browserCallables);
        var rootNode = new RootNode(browserCallables, storage.getOpenAPI());
        var pluginManager = new PluginManager(
                storage.getParserConfig().getPlugins());
        pluginManager.setStorage(storage);
        var pluginExecutor = new PluginExecutor(pluginManager, rootNode);
        pluginExecutor.execute();

        logger.debug("JVM Parser finished successfully");

        return storage.getOpenAPI();
    }

    private void validateEndpointExposedClassesForAclAnnotations(
            List<Class<?>> browserCallables) {

        browserCallables.stream().flatMap(Parser::getSuperclasses)
                .flatMap(browserCallable -> config
                        .getEndpointExposedAnnotations().stream()
                        .map(ann -> List.of(browserCallable, ann)))
                .filter(pair -> pair.get(0).isAnnotationPresent(
                        (Class<? extends Annotation>) pair.get(1)))
                .forEach(pair -> {
                    checkClassLevelAnnotation(pair.get(0), pair.get(1));
                    checkMethodLevelAnnotation(pair.get(0), pair.get(1));
                });
    }

    private static Stream<Class<?>> getSuperclasses(Class<?> clazz) {
        return Stream.iterate(clazz.getSuperclass(), Objects::nonNull,
                Class::getSuperclass);
    }

    private void checkClassLevelAnnotation(Class<?> browserCallable,
            Class<?> exposedAnnotation) {
        Arrays.stream(browserCallable.getAnnotations())
                .forEach(annotationInfo -> throwIfAnnotationIsAclAnnotation(
                        annotationInfo.annotationType().getName(),
                        browserCallable, exposedAnnotation));
    }

    private void checkMethodLevelAnnotation(Class<?> browserCallable,
            Class<?> exposedAnnotation) {
        for (Method method : browserCallable.getMethods()) {
            var annotations = method.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                throwIfAnnotationIsAclAnnotation(
                        annotation.annotationType().getName(), browserCallable,
                        exposedAnnotation);
            }
        }
    }

    private void throwIfAnnotationIsAclAnnotation(String annotationName,
            Class<?> browserCallable, Class<?> exposedAnnotation) {
        if (ACL_ANNOTATIONS.contains(annotationName)) {
            throw new ParserException(String.format(
                    ENDPOINT_EXPOSED_AND_ACL_ANNOTATIONS_ERROR_TEMPLATE,
                    browserCallable.getName(), exposedAnnotation.getName(),
                    annotationName, exposedAnnotation.getName()));
        }
    }

    /**
     * Gets the internal configuration object.
     *
     * @return configuration object.
     */
    @NonNull
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
    @NonNull
    public Parser openAPISource(@NonNull String source,
            @NonNull OpenAPIFileType type) {
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
    @NonNull
    public Parser plugins(@NonNull Plugin... plugins) {
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
    @NonNull
    public Parser plugins(@NonNull Collection<? extends Plugin> plugins) {
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
        private List<Class<? extends Annotation>> endpointAnnotations = List
                .of();
        private List<Class<? extends Annotation>> endpointExposedAnnotations = List
                .of();
        private OpenAPI openAPI;

        private Config(OpenAPI openAPI) {
            this.openAPI = openAPI;
        }

        /**
         * Gets the collection of classpath elements.
         *
         * @return the collection of classpath elements.
         */
        @NonNull
        public Set<String> getClassPathElements() {
            return classPathElements;
        }

        /**
         * Gets the name of endpoint annotation.
         *
         * @return the annotation name.
         */
        @NonNull
        public List<Class<? extends Annotation>> getEndpointAnnotations() {
            return endpointAnnotations;
        }

        /**
         * Gets the name of `EndpointExposed` annotation.
         *
         * @return the annotation name.
         */
        @NonNull
        public List<Class<? extends Annotation>> getEndpointExposedAnnotations() {
            return endpointExposedAnnotations;
        }

        /**
         * Gets the OpenAPI object.
         *
         * <p>
         * Note that the object is mutable.
         *
         * @return OpenAPI object.
         */
        @NonNull
        public OpenAPI getOpenAPI() {
            return openAPI;
        }

        /**
         * Returns a collection of parser plugins.
         *
         * @return the collection of parser plugins.
         */
        @NonNull
        public Collection<Plugin> getPlugins() {
            return plugins;
        }

    }
}
