package com.vaadin.hilla.engine;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for the generator engine. It exposes all properties that can be
 * overridden by an alternative implementation, while providing defaults for all
 * of them.
 *
 * @see #load() for details on how to override the default configuration
 */
public interface EngineConfiguration {
    /**
     * The internal configuration state. It is recommended to use
     * {@link #load()} to get the current configuration.
     */
    EngineConfiguration STATE = new EngineConfiguration() {
    };

    Logger LOGGER = LoggerFactory.getLogger(EngineConfiguration.class);

    String OPEN_API_PATH = "hilla-openapi.json";

    /**
     * Gets the output directory for the generated files.
     */
    default Path getOutputDir() {
        if (State.INSTANCE.outputDir != null) {
            return State.INSTANCE.outputDir;
        }

        var legacyFrontendDir = getBaseDir().resolve("frontend");

        if (Files.exists(legacyFrontendDir)) {
            return legacyFrontendDir.resolve("generated");
        } else {
            return getBaseDir().resolve(
                    FrontendUtils.DEFAULT_PROJECT_FRONTEND_GENERATED_DIR);
        }
    }

    /**
     * Gets the classpath to be used by the engine.
     */
    default Set<Path> getClasspath() {
        return State.INSTANCE.classpath;
    }

    /**
     * Gets the Maven group ID of the project.
     */
    default String getGroupId() {
        return State.INSTANCE.groupId;
    }

    /**
     * Gets the Maven artifact ID of the project.
     */
    default String getArtifactId() {
        return State.INSTANCE.artifactId;
    }

    /**
     * Gets the main class of the project. This is only useful in some
     * applications where the main class cannot be determined automatically.
     */
    default String getMainClass() {
        return State.INSTANCE.mainClass;
    }

    /**
     * Gets the build directory of the project.
     */
    default Path getBuildDir() {
        return State.INSTANCE.buildDir == null ? getBaseDir().resolve("target")
                : State.INSTANCE.buildDir;
    }

    /**
     * Gets the root directory of the project.
     */
    default Path getBaseDir() {
        return State.INSTANCE.baseDir;
    }

    /**
     * Gets the directories where the compiled classes are located.
     */
    default List<Path> getClassesDirs() {
        return State.INSTANCE.classesDirs == null
                ? List.of(getBuildDir().resolve("classes"))
                : State.INSTANCE.classesDirs;
    }

    /**
     * Gets the configuration specific to the generator.
     */
    default GeneratorConfiguration getGenerator() {
        return State.INSTANCE.generator;
    }

    /**
     * Gets the configuration specific to the parser.
     */
    default ParserConfiguration getParser() {
        return State.INSTANCE.parser;
    }

    /**
     * Checks if the application is running in production mode.
     */
    default boolean isProductionMode() {
        return State.INSTANCE.productionMode;
    }

    /**
     * Gets the command to run Node.js.
     */
    default String getNodeCommand() {
        return State.INSTANCE.nodeCommand;
    }

    /**
     * Gets the class finder to be used by the engine.
     */
    default ClassFinder getClassFinder() {
        return State.INSTANCE.classFinder;
    }

    /**
     * Gets the names of the annotations that mark the browser callables.
     */
    default List<String> getEndpointAnnotationNames() {
        return State.INSTANCE.endpointAnnotationNames;
    }

    /**
     * Gets the names of the annotations that mark the browser callables.
     */
    default List<Class<? extends Annotation>> getEndpointAnnotations() {
        return getEndpointAnnotationNames().stream()
                .map(this::toAnnotationClass).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    default List<String> getEndpointExposedAnnotationNames() {
        return State.INSTANCE.endpointExposedAnnotationNames;
    }

    default List<Class<? extends Annotation>> getEndpointExposedAnnotations() {
        return getEndpointExposedAnnotationNames().stream()
                .map(this::toAnnotationClass).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    default Path getOpenAPIFile() {
        if (State.INSTANCE.openAPIFile != null) {
            return State.INSTANCE.openAPIFile;
        }

        return isProductionMode()
                ? getClassesDirs().get(0).resolve(OPEN_API_PATH)
                : getBuildDir().resolve(OPEN_API_PATH);
    }

    default List<BrowserCallableFinder> getBrowserCallableFinders() {
        if (State.INSTANCE.browserCallableFinders != null
                && !State.INSTANCE.browserCallableFinders.isEmpty()) {
            return State.INSTANCE.browserCallableFinders;
        }

        return getClassFinder() == null
                ? List.of(AotBrowserCallableFinder::find)
                : List.of(LookupBrowserCallableFinder::find,
                        AotBrowserCallableFinder::find);
    }

    static EngineConfiguration load() {
        var configurations = ServiceLoader.load(EngineConfiguration.class)
                .stream().map(ServiceLoader.Provider::get).toList();

        if (configurations.size() > 1) {
            throw new ConfigurationException(configurations.stream()
                    .map(config -> config.getClass().getName())
                    .collect(Collectors.joining("\", \"",
                            "Multiple EngineConfiguration instances found: \"",
                            "\"")));
        }

        return configurations.isEmpty() ? STATE : configurations.get(0);
    }

    default EngineConfiguration setBaseDir(Path baseDir) {
        State.INSTANCE.baseDir = baseDir;
        return this;
    }

    default EngineConfiguration setBuildDir(String buildDir) {
        setBuildDir(getBaseDir().resolve(buildDir));
        return this;
    }

    default EngineConfiguration setBuildDir(Path buildDir) {
        State.INSTANCE.buildDir = buildDir;
        return this;
    }

    default EngineConfiguration setClassesDirs(Path... classesDirs) {
        State.INSTANCE.classesDirs = Arrays.asList(classesDirs);
        return this;
    }

    default EngineConfiguration setClasspath(Set<Path> classpath) {
        State.INSTANCE.classpath = classpath;
        return this;
    }

    default EngineConfiguration setClasspath(Collection<String> value) {
        setClasspath(value.stream().map(Path::of).map(this::resolve)
                .collect(Collectors.toSet()));
        return this;
    }

    default EngineConfiguration setGenerator(GeneratorConfiguration generator) {
        State.INSTANCE.generator = generator;
        return this;
    }

    default EngineConfiguration setOutputDir(String outputDir) {
        setOutputDir(getBaseDir().resolve(outputDir));
        return this;
    }

    default EngineConfiguration setOutputDir(Path outputDir) {
        State.INSTANCE.outputDir = outputDir;
        return this;
    }

    default EngineConfiguration setParser(ParserConfiguration parser) {
        State.INSTANCE.parser = parser;
        return this;
    }

    default EngineConfiguration setGroupId(String groupId) {
        State.INSTANCE.groupId = groupId;
        return this;
    }

    default EngineConfiguration setArtifactId(String artifactId) {
        State.INSTANCE.artifactId = artifactId;
        return this;
    }

    default EngineConfiguration setMainClass(String mainClass) {
        State.INSTANCE.mainClass = mainClass;
        return this;
    }

    default EngineConfiguration setBrowserCallableFinders(
            BrowserCallableFinder... browserCallableFinders) {
        State.INSTANCE.browserCallableFinders = Arrays
                .asList(browserCallableFinders);
        return this;
    }

    default List<Class<?>> findBrowserCallables()
            throws ExecutionFailedException {
        var iterator = getBrowserCallableFinders().iterator();

        while (iterator.hasNext()) {
            var finder = iterator.next();

            try {
                return finder.find(this);
            } catch (ExecutionFailedException e) {
                if (iterator.hasNext()) {
                    LOGGER.debug("Failed to find browser-callables", e);
                } else {
                    throw e;
                }
            }
        }

        // should never happen, as the last one throws
        throw new IllegalStateException(
                "No other browser-callable finders available");
    }

    default EngineConfiguration setProductionMode(boolean productionMode) {
        State.INSTANCE.productionMode = productionMode;
        return this;
    }

    default EngineConfiguration setNodeCommand(String nodeCommand) {
        State.INSTANCE.nodeCommand = nodeCommand;
        return this;
    }

    default EngineConfiguration setClassFinder(ClassFinder classFinder) {
        State.INSTANCE.classFinder = classFinder;
        return this;
    }

    default EngineConfiguration setEndpointAnnotationNames(
            String... endpointAnnotationNames) {
        State.INSTANCE.endpointAnnotationNames = Arrays
                .asList(endpointAnnotationNames);
        return this;
    }

    default EngineConfiguration setEndpointExposedAnnotationNames(
            String... endpointExposedAnnotationNames) {
        State.INSTANCE.endpointExposedAnnotationNames = Arrays
                .asList(endpointExposedAnnotationNames);
        return this;
    }

    default EngineConfiguration setOpenAPIFile(Path openAPIFile) {
        State.INSTANCE.openAPIFile = openAPIFile;
        return this;
    }

    default ClassLoader getClassLoader() {
        if (State.INSTANCE.classLoader == null && getClassFinder() != null) {
            State.INSTANCE.classLoader = getClassFinder().getClassLoader();
        }

        if (State.INSTANCE.classLoader == null) {
            var urls = getClasspath().stream().map(path -> {
                try {
                    return path.toUri().toURL();
                } catch (MalformedURLException e) {
                    throw new ConfigurationException(
                            "Classpath contains invalid elements", e);
                }
            }).toArray(URL[]::new);
            State.INSTANCE.classLoader = new URLClassLoader(urls,
                    getClass().getClassLoader());
        }

        return State.INSTANCE.classLoader;
    }

    @SuppressWarnings("unchecked")
    default Class<? extends Annotation> toAnnotationClass(String name) {
        try {
            var c = Class.forName(name, true, getClassLoader());

            if (c.isAnnotation()) {
                return (Class<? extends Annotation>) c;
            }

            LOGGER.debug("Class {} is not an annotation", name);
        } catch (Throwable t) { // in some cases an error can be thrown
            LOGGER.debug("Class not found for annotation " + name, t);
        }

        return null;
    }

    default Path resolve(Path path) {
        return path.isAbsolute() ? path.normalize()
                : getBaseDir().resolve(path).normalize();
    }

    static void reset() {
        State.INSTANCE = new State();
    }
}

class State {
    static State INSTANCE = new State();

    Set<Path> classpath;
    ClassLoader classLoader;
    String groupId;
    String artifactId;
    String mainClass;
    Path baseDir;
    Path buildDir;
    Path openAPIFile;
    List<Path> classesDirs;
    List<String> endpointAnnotationNames;
    List<String> endpointExposedAnnotationNames;
    GeneratorConfiguration generator;
    Path outputDir;
    ParserConfiguration parser;
    List<BrowserCallableFinder> browserCallableFinders;
    boolean productionMode;
    String nodeCommand;
    ClassFinder classFinder;

    State() {
        classpath = Arrays
                .stream(System.getProperty("java.class.path")
                        .split(File.pathSeparator))
                .map(Path::of).collect(Collectors.toSet());
        baseDir = Path.of(System.getProperty("user.dir"));
        endpointAnnotationNames = List.of("com.vaadin.hilla.BrowserCallable",
                "com.vaadin.hilla.Endpoint");
        endpointExposedAnnotationNames = List
                .of("com.vaadin.hilla.EndpointExposed");
        generator = new GeneratorConfiguration();
        parser = new ParserConfiguration();
        nodeCommand = "node";
    }
}
