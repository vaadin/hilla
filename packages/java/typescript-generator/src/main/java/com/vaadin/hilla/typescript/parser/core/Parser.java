package com.vaadin.hilla.typescript.parser.core;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.hilla.typescript.codegen.ParserOutput;
import com.vaadin.hilla.typescript.codegen.TypeScriptGenerator;
import com.vaadin.hilla.typescript.codegen.plugins.BarrelPlugin;
import com.vaadin.hilla.typescript.codegen.plugins.ClientPlugin;
import com.vaadin.hilla.typescript.codegen.plugins.ModelPlugin;
import com.vaadin.hilla.typescript.parser.models.ClassInfoModel;

/**
 * The entrypoint class for TypeScript generation. It searches for endpoint
 * classes in the classpath and generates TypeScript client code directly from
 * Java classes, without using OpenAPI as an intermediate format.
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
        this.config = new Config();
    }

    /**
     * Specifies the classpath which the parser will scan for the classes.
     *
     * @param classPathElements
     *            A collection of paths in a form of strings
     * @return this (for method chaining).
     */
    @NonNull
    public Parser classPath(@NonNull String... classPathElements) {
        return classPath(classPathElements, true);
    }

    /**
     * Specifies the classpath which the parser will scan for the classes.
     *
     * @param classPathElements
     *            A collection of paths in a form of strings
     * @param override
     *            specifies if the parser should override the classpath if it is
     *            already specified.
     * @return this (for method chaining).
     */
    @NonNull
    public Parser classPath(@NonNull String[] classPathElements,
            boolean override) {
        return classPath(Arrays.asList(classPathElements), override);
    }

    /**
     * Specifies the classpath which the parser will scan for the classes.
     *
     * @param classPathElements
     *            A collection of paths in a form of strings
     * @return this (for method chaining).
     */
    @NonNull
    public Parser classPath(@NonNull Collection<String> classPathElements) {
        return classPath(classPathElements, true);
    }

    /**
     * Specifies the classpath which the parser will scan for the classes.
     *
     * @param classPathElements
     *            A collection of paths in a form of strings
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
     * Generates TypeScript client code directly from Java classes, without using
     * OpenAPI as an intermediate format. This is the new direct Java-to-TypeScript
     * generation approach.
     *
     * @param browserCallables the browser callable endpoint classes
     * @param outputDir        the output directory for generated TypeScript
     *                         files
     * @throws IOException if an error occurs while writing files
     */
    public void generateTypeScript(@NonNull List<Class<?>> browserCallables,
            @NonNull Path outputDir) throws IOException {
        Objects.requireNonNull(config.classPathElements,
                "[JVM Parser] classPath is not provided.");
        if (config.endpointAnnotations == null
                || config.endpointAnnotations.isEmpty()) {
            throw new IllegalArgumentException(
                    "[JVM Parser] endpoint annotations are not provided.");
        }

        logger.debug("Generating TypeScript directly from Java classes");

        // Filter out internal browser callables
        browserCallables = browserCallables.stream().filter(
                cls -> !INTERNAL_BROWSER_CALLABLES.contains(cls.getName()))
                .toList();

        // Validate annotations
        validateEndpointExposedClassesForAclAnnotations(browserCallables);

        // Convert Class<?> to ClassInfoModel for endpoints
        List<ClassInfoModel> endpoints = browserCallables.stream()
                .map(ClassInfoModel::of).toList();

        // For now, use empty list for entities
        // TODO: Collect all classes referenced by endpoints
        List<ClassInfoModel> entities = List.of();

        // Create ParserOutput
        ParserOutput parserOutput = new ParserOutput(endpoints, entities);

        // Create TypeScript generator with plugins (in order of execution)
        TypeScriptGenerator generator = new TypeScriptGenerator(
                outputDir.toString());
        generator.addPlugin(new com.vaadin.hilla.typescript.codegen.plugins.TransferTypesPlugin());
        generator.addPlugin(new ModelPlugin());
        generator.addPlugin(new com.vaadin.hilla.typescript.codegen.plugins.SubtypesPlugin());
        generator.addPlugin(new ClientPlugin());
        generator.addPlugin(new com.vaadin.hilla.typescript.codegen.plugins.PushPlugin());
        generator.addPlugin(new com.vaadin.hilla.typescript.codegen.plugins.SignalsPlugin());
        generator.addPlugin(new BarrelPlugin());

        // Generate and write TypeScript files
        generator.generateAndWrite(parserOutput);

        logger.debug("TypeScript generation finished successfully");
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
     * A configuration class used by the parser. It holds the necessary
     * parameters for TypeScript generation.
     */
    public static final class Config {
        private Set<String> classPathElements;
        private List<Class<? extends Annotation>> endpointAnnotations;
        private List<Class<? extends Annotation>> endpointExposedAnnotations;

        Config() {
            this.endpointAnnotations = new ArrayList<>();
            this.endpointExposedAnnotations = new ArrayList<>();
        }

        /**
         * Gets a list of classes related to the parsed endpoints.
         *
         * @return a set of strings with classpath elements
         */
        public Set<String> getClassPathElements() {
            return classPathElements == null ? Set.of()
                    : new HashSet<>(classPathElements);
        }

        /**
         * Gets the list of endpoint annotations that the parser should look for.
         *
         * @return a list of annotation classes
         */
        public List<Class<? extends Annotation>> getEndpointAnnotations() {
            return new ArrayList<>(endpointAnnotations);
        }

        /**
         * Gets the list of endpoint exposed annotations.
         *
         * @return a list of annotation classes
         */
        public List<Class<? extends Annotation>> getEndpointExposedAnnotations() {
            return new ArrayList<>(endpointExposedAnnotations);
        }
    }
}
