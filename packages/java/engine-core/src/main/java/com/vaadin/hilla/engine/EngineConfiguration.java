package com.vaadin.hilla.engine;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.FrontendUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineConfiguration {
    private static EngineConfiguration INSTANCE;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(EngineConfiguration.class);

    public static final String OPEN_API_PATH = "hilla-openapi.json";
    private Set<Path> classpath = Arrays
            .stream(System.getProperty("java.class.path")
                    .split(File.pathSeparator))
            .map(Path::of).collect(Collectors.toSet());
    private String groupId;
    private String artifactId;
    private String mainClass;
    private Path buildDir;
    private Path baseDir;
    private Path classesDir;
    private GeneratorConfiguration generator;
    private Path outputDir;
    private ParserConfiguration parser;
    private EndpointFinder endpointFinder;
    private boolean productionMode = false;
    private String nodeCommand = "node";

    private EngineConfiguration() {
        baseDir = Path.of(System.getProperty("user.dir"));
        buildDir = baseDir.resolve("target");
        generator = new GeneratorConfiguration();
        parser = new ParserConfiguration();

        var legacyFrontendGeneratedDir = baseDir.resolve("frontend/generated");
        if (Files.exists(legacyFrontendGeneratedDir)) {
            outputDir = legacyFrontendGeneratedDir;
        } else {
            outputDir = baseDir.resolve(
                    FrontendUtils.DEFAULT_PROJECT_FRONTEND_GENERATED_DIR);
        }
    }

    public Set<Path> getClasspath() {
        return classpath;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getMainClass() {
        return mainClass;
    }

    public Path getBuildDir() {
        return buildDir;
    }

    public Path getBaseDir() {
        return baseDir;
    }

    public Path getClassesDir() {
        return classesDir == null ? buildDir.resolve("classes") : classesDir;
    }

    public GeneratorConfiguration getGenerator() {
        return generator;
    }

    public Path getOutputDir() {
        return outputDir;
    }

    public ParserConfiguration getParser() {
        return parser;
    }

    public boolean isProductionMode() {
        return productionMode;
    }

    public String getNodeCommand() {
        return nodeCommand;
    }

    public List<Class<? extends Annotation>> getEndpointAnnotations() {
        return parser.getEndpointAnnotations();
    }

    public List<Class<? extends Annotation>> getEndpointExposedAnnotations() {
        return parser.getEndpointExposedAnnotations();
    }

    public Path getOpenAPIFile() {
        return productionMode
                ? buildDir.resolve("classes").resolve(OPEN_API_PATH)
                : buildDir.resolve(OPEN_API_PATH);
    }

    public EndpointFinder getEndpointFinder() {
        if (endpointFinder != null) {
            return endpointFinder;
        }

        return () -> {
            try {
                return AotEndpointFinder.findEndpointClasses(this);
            } catch (IOException | InterruptedException e) {
                throw new ExecutionFailedException(e);
            }
        };
    }

    public static EngineConfiguration getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new EngineConfiguration();
            try {
                INSTANCE.parser.setEndpointAnnotations(List.of(
                        (Class<? extends Annotation>) Class
                                .forName("com.vaadin.hilla.BrowserCallable"),
                        (Class<? extends Annotation>) Class
                                .forName("com.vaadin.hilla.Endpoint")));
                INSTANCE.parser.setEndpointExposedAnnotations(
                        List.of((Class<? extends Annotation>) Class
                                .forName("com.vaadin.hilla.EndpointExposed")));
            } catch (Throwable t) {
                LOGGER.warn("Default annotations not found", t);
            }
        }

        return INSTANCE;
    }

    public static void setDefault(EngineConfiguration config) {
        INSTANCE = config;
    }

    public static final class Builder {
        private final EngineConfiguration configuration = new EngineConfiguration();

        public Builder() {
            this(getDefault());
        }

        public Builder(EngineConfiguration configuration) {
            this.configuration.baseDir = configuration.baseDir;
            this.configuration.buildDir = configuration.buildDir;
            this.configuration.classesDir = configuration.classesDir;
            this.configuration.classpath = configuration.classpath;
            this.configuration.generator = configuration.generator;
            this.configuration.parser = configuration.parser;
            this.configuration.outputDir = configuration.outputDir;
            this.configuration.groupId = configuration.groupId;
            this.configuration.artifactId = configuration.artifactId;
            this.configuration.mainClass = configuration.mainClass;
            this.configuration.endpointFinder = configuration.endpointFinder;
            this.configuration.productionMode = configuration.productionMode;
            this.configuration.nodeCommand = configuration.nodeCommand;
            this.configuration.parser.setEndpointAnnotations(
                    configuration.getEndpointAnnotations());
            this.configuration.parser.setEndpointExposedAnnotations(
                    configuration.getEndpointExposedAnnotations());
        }

        public Builder baseDir(Path value) {
            configuration.baseDir = value;
            return this;
        }

        public Builder buildDir(String value) {
            return buildDir(Path.of(value));
        }

        public Builder buildDir(Path value) {
            configuration.buildDir = resolve(value);
            return this;
        }

        public Builder classesDir(Path value) {
            configuration.classesDir = resolve(value);
            return this;
        }

        public Builder classpath(Collection<String> value) {
            configuration.classpath = value.stream().map(Path::of)
                    .map(this::resolve).collect(Collectors.toSet());
            return this;
        }

        public EngineConfiguration create() {
            return configuration;
        }

        public Builder generator(GeneratorConfiguration value) {
            configuration.generator = value;
            return this;
        }

        public Builder outputDir(String value) {
            return outputDir(Path.of(value));
        }

        public Builder outputDir(Path value) {
            configuration.outputDir = resolve(value);
            return this;
        }

        public Builder parser(ParserConfiguration value) {
            configuration.parser = value;
            return this;
        }

        public Builder groupId(String value) {
            configuration.groupId = value;
            return this;
        }

        public Builder artifactId(String value) {
            configuration.artifactId = value;
            return this;
        }

        public Builder mainClass(String value) {
            configuration.mainClass = value;
            return this;
        }

        public Builder endpointFinder(EndpointFinder value) {
            configuration.endpointFinder = value;
            return this;
        }

        public Builder productionMode(boolean value) {
            configuration.productionMode = value;
            return this;
        }

        public Builder nodeCommand(String value) {
            configuration.nodeCommand = value;
            return this;
        }

        public Builder endpointAnnotations(
                Class<? extends Annotation>... value) {
            configuration.parser.setEndpointAnnotations(Arrays.asList(value));
            return this;
        }

        public Builder endpointExposedAnnotations(
                Class<? extends Annotation>... value) {
            configuration.parser
                    .setEndpointExposedAnnotations(Arrays.asList(value));
            return this;
        }

        private Path resolve(Path path) {
            return path.isAbsolute() ? path.normalize()
                    : configuration.baseDir.resolve(path).normalize();
        }
    }

    @FunctionalInterface
    public interface EndpointFinder {
        List<Class<?>> findEndpoints() throws ExecutionFailedException;
    }
}
