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
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface EngineConfiguration {
    EngineConfiguration DEFAULT = new EngineConfiguration() {
    };
    Logger LOGGER = LoggerFactory.getLogger(EngineConfiguration.class);

    String OPEN_API_PATH = "hilla-openapi.json";

    default Path getOutputDir() {
        if (State.outputDir == null) {
            var legacyFrontendDir = getBaseDir().resolve("frontend");

            if (Files.exists(legacyFrontendDir)) {
                State.outputDir = legacyFrontendDir.resolve("generated");
            } else {
                State.outputDir = getBaseDir().resolve(
                        FrontendUtils.DEFAULT_PROJECT_FRONTEND_GENERATED_DIR);
            }
        }

        return State.outputDir;
    }

    default Set<Path> getClasspath() {
        return State.classpath;
    }

    default String getGroupId() {
        return State.groupId;
    }

    default String getArtifactId() {
        return State.artifactId;
    }

    default String getMainClass() {
        return State.mainClass;
    }

    default Path getBuildDir() {
        return State.buildDir;
    }

    default Path getBaseDir() {
        return State.baseDir;
    }

    default List<Path> getClassesDirs() {
        return State.classesDirs;
    }

    default GeneratorConfiguration getGenerator() {
        return State.generator;
    }

    default ParserConfiguration getParser() {
        return State.parser;
    }

    default boolean isProductionMode() {
        return State.productionMode;
    }

    default String getNodeCommand() {
        return State.nodeCommand;
    }

    default ClassFinder getClassFinder() {
        return State.classFinder;
    }

    default List<Class<? extends Annotation>> getEndpointAnnotations() {
        return State.parser.getEndpointAnnotations();
    }

    default List<Class<? extends Annotation>> getEndpointExposedAnnotations() {
        return State.parser.getEndpointExposedAnnotations();
    }

    default Path getOpenAPIFile() {
        return isProductionMode()
                ? getClassesDirs().stream()
                        .map(dir -> dir.resolve(OPEN_API_PATH))
                        .filter(Files::isRegularFile).findFirst()
                        .orElse(getClassesDirs().get(0).resolve(OPEN_API_PATH))
                : getBuildDir().resolve(OPEN_API_PATH);
    }

    default BrowserCallableFinder getBrowserCallableFinder() {
        if (State.browserCallableFinder != null) {
            return State.browserCallableFinder;
        }

        return () -> {
            try {
                return AotBrowserCallableFinder.findEndpointClasses(this);
            } catch (Exception e) {
                if (this.getClassFinder() != null) {
                    LOGGER.info(
                            "AOT-based detection of browser-callable classes failed."
                                    + " Falling back to classpath scan."
                                    + " Enable debug logging for more information.");
                    return LookupBrowserCallableFinder
                            .findEndpointClasses(this);
                } else {
                    throw new ExecutionFailedException(e);
                }
            }
        };
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

        return configurations.isEmpty() ? DEFAULT : configurations.get(0);
    }

    default EngineConfiguration setBaseDir(Path baseDir) {
        State.baseDir = baseDir;
        return this;
    }

    default EngineConfiguration setBuildDir(String buildDir) {
        setBuildDir(Path.of(buildDir));
        return this;
    }

    default EngineConfiguration setBuildDir(Path buildDir) {
        State.buildDir = buildDir;
        return this;
    }

    default EngineConfiguration setClassesDirs(List<Path> classesDirs) {
        State.classesDirs = classesDirs;
        return this;
    }

    default EngineConfiguration setClasspath(Set<Path> classpath) {
        State.classpath = classpath;
        return this;
    }

    default EngineConfiguration setClasspath(Collection<String> value) {
        setClasspath(value.stream().map(Path::of).map(this::resolve)
                .collect(Collectors.toSet()));
        return this;
    }

    default EngineConfiguration setGenerator(GeneratorConfiguration generator) {
        State.generator = generator;
        return this;
    }

    default EngineConfiguration setOutputDir(String outputDir) {
        setOutputDir(Path.of(outputDir));
        return this;
    }

    default EngineConfiguration setOutputDir(Path outputDir) {
        State.outputDir = outputDir;
        return this;
    }

    default EngineConfiguration setParser(ParserConfiguration parser) {
        State.parser = parser;
        return this;
    }

    default EngineConfiguration setGroupId(String groupId) {
        State.groupId = groupId;
        return this;
    }

    default EngineConfiguration setArtifactId(String artifactId) {
        State.artifactId = artifactId;
        return this;
    }

    default EngineConfiguration setMainClass(String mainClass) {
        State.mainClass = mainClass;
        return this;
    }

    default EngineConfiguration setBrowserCallableFinder(
            BrowserCallableFinder browserCallableFinder) {
        State.browserCallableFinder = browserCallableFinder;
        return this;
    }

    default EngineConfiguration setProductionMode(boolean productionMode) {
        State.productionMode = productionMode;
        return this;
    }

    default EngineConfiguration setNodeCommand(String nodeCommand) {
        State.nodeCommand = nodeCommand;
        return this;
    }

    default EngineConfiguration setClassFinder(ClassFinder classFinder) {
        State.classFinder = classFinder;
        return this;
    }

    default EngineConfiguration setEndpointAnnotations(
            Class<? extends Annotation>... endpointAnnotations) {
        State.parser.setEndpointAnnotations(Arrays.asList(endpointAnnotations));
        return this;
    }

    default EngineConfiguration setEndpointExposedAnnotations(
            Class<? extends Annotation>... endpointExposedAnnotations) {
        State.parser.setEndpointExposedAnnotations(
                Arrays.asList(endpointExposedAnnotations));
        return this;
    }

    default ClassLoader getClassLoader() {
        if (State.classLoader == null) {
            var urls = getClasspath().stream().map(path -> {
                try {
                    return path.toUri().toURL();
                } catch (MalformedURLException e) {
                    throw new ConfigurationException(
                            "Classpath contains invalid elements", e);
                }
            }).toArray(URL[]::new);
            State.classLoader = new URLClassLoader(urls,
                    getClass().getClassLoader());
        }
        return State.classLoader;
    }

    default EngineConfiguration withDefaultAnnotations() {
        try {
            setEndpointAnnotations((Class<? extends Annotation>) Class.forName(
                    "com.vaadin.hilla.BrowserCallable", true, getClassLoader()),
                    (Class<? extends Annotation>) Class.forName(
                            "com.vaadin.hilla.Endpoint", true,
                            getClassLoader()));
            setEndpointExposedAnnotations((Class<? extends Annotation>) Class
                    .forName("com.vaadin.hilla.EndpointExposed", true,
                            getClassLoader()));
        } catch (Throwable t) {
            LOGGER.debug(
                    "Default annotations not found. Hilla is probably not in the classpath.");
        }
        return this;
    }

    default Path resolve(Path path) {
        return path.isAbsolute() ? path.normalize()
                : getBaseDir().resolve(path).normalize();
    }
}

class State {
    static Set<Path> classpath = Arrays
            .stream(System.getProperty("java.class.path")
                    .split(File.pathSeparator))
            .map(Path::of).collect(Collectors.toSet());
    static ClassLoader classLoader;
    static String groupId;
    static String artifactId;
    static String mainClass;
    static Path baseDir = Path.of(System.getProperty("user.dir"));
    static Path buildDir = baseDir.resolve("target");
    static List<Path> classesDirs = List.of(buildDir.resolve("classes"));
    static GeneratorConfiguration generator = new GeneratorConfiguration();
    static Path outputDir;
    static ParserConfiguration parser = new ParserConfiguration();
    static BrowserCallableFinder browserCallableFinder;
    static boolean productionMode = false;
    static String nodeCommand = "node";
    static ClassFinder classFinder;
}
