package com.vaadin.hilla.engine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vaadin.flow.server.frontend.FrontendUtils;

public class EngineConfiguration {
    public static final String OPEN_API_PATH = "hilla-openapi.json";
    public static String classpath = System.getProperty("java.class.path");
    public static String groupId;
    public static String artifactId;
    public static String mainClass;
    public static Path buildDir;
    private Path baseDir;
    @JsonDeserialize(as = LinkedHashSet.class)
    private Set<Path> classPath;
    private Path classesDir;
    private GeneratorConfiguration generator;
    private Path outputDir;
    private ParserConfiguration parser;

    public EngineConfiguration() {
        baseDir = Path.of(System.getProperty("user.dir"));
        buildDir = baseDir.resolve("target");
        classesDir = buildDir.resolve("classes");
        generator = new GeneratorConfiguration();
        parser = new ParserConfiguration();
        classPath = new LinkedHashSet<>();

        var legacyFrontendGeneratedDir = baseDir.resolve("frontend/generated");
        if (Files.exists(legacyFrontendGeneratedDir)) {
            outputDir = legacyFrontendGeneratedDir;
        } else {
            outputDir = baseDir.resolve(
                    FrontendUtils.DEFAULT_PROJECT_FRONTEND_GENERATED_DIR);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (EngineConfiguration) o;
        return Objects.equals(baseDir, that.baseDir)
                && Objects.equals(classPath, that.classPath)
                && Objects.equals(generator, that.generator)
                && Objects.equals(parser, that.parser)
                && Objects.equals(classesDir, that.classesDir)
                && Objects.equals(outputDir, that.outputDir);
    }

    public Path getBaseDir() {
        return baseDir;
    }

    public Path getBuildDir() {
        return buildDir;
    }

    public Set<Path> getClassPath() {
        return classPath;
    }

    public Path getClassesDir() {
        return classesDir;
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

    @Override
    public int hashCode() {
        return Objects.hash(baseDir, classPath, generator, parser, classesDir,
                outputDir);
    }

    @JsonIgnore
    public Path getOpenAPIFile(boolean isProductionMode) {
        return isProductionMode ? classesDir.resolve(OPEN_API_PATH)
                : buildDir.resolve(OPEN_API_PATH);
    }

    public static final class Builder {
        private final EngineConfiguration configuration = new EngineConfiguration();

        public Builder(Path baseDir) {
            configuration.baseDir = baseDir;
            var legacyFrontendGeneratedDir = baseDir
                    .resolve("frontend/generated");
            if (Files.exists(legacyFrontendGeneratedDir)) {
                configuration.outputDir = legacyFrontendGeneratedDir;
            } else {
                configuration.outputDir = baseDir.resolve(
                        FrontendUtils.DEFAULT_PROJECT_FRONTEND_GENERATED_DIR);
            }
        }

        public Builder(EngineConfiguration configuration) {
            this.configuration.baseDir = configuration.baseDir;
            this.configuration.classPath = configuration.classPath;
            this.configuration.generator = configuration.generator;
            this.configuration.parser = configuration.parser;
            this.configuration.classesDir = configuration.classesDir;
            this.configuration.outputDir = configuration.outputDir;
        }

        public Builder baseDir(Path value) {
            configuration.baseDir = value;
            return this;
        }

        public Builder buildDir(String value) {
            return buildDir(Path.of(value));
        }

        public Builder buildDir(Path value) {
            buildDir = resolve(value);
            return this;
        }

        public Builder classPath(Collection<String> value) {
            configuration.classPath = value.stream().map(Path::of)
                    .map(this::resolve)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            return this;
        }

        public Builder classesDir(Path value) {
            configuration.classesDir = resolve(value);
            return this;
        }

        public Builder classesDir(String value) {
            return classesDir(Path.of(value));
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

        private Path resolve(Path path) {
            return path.isAbsolute() ? path.normalize()
                    : configuration.baseDir.resolve(path).normalize();
        }
    }
}
