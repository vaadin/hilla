package com.vaadin.hilla.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vaadin.flow.server.frontend.FrontendUtils;

public class EngineConfiguration {
    public static final String DEFAULT_CONFIG_FILE_NAME = "hilla-engine-configuration.json";
    public static final String OPEN_API_PATH = "hilla-openapi.json";
    static final ObjectMapper MAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD,
                    JsonAutoDetect.Visibility.ANY);
    private Path baseDir;
    private Path buildDir;
    @JsonDeserialize(as = LinkedHashSet.class)
    private Set<Path> classPath;
    private Path classesDir;
    private GeneratorConfiguration generator;
    private Path outputDir;
    private ParserConfiguration parser;

    private EngineConfiguration() {
    }

    /**
     * Reads the configuration from the given base directory. Reads only files
     * with the default name.
     *
     * @param configDir
     *            a directory that contains the configuration file.
     * @return the configuration, or <code>null</code> if the configuration file
     *         does not exist
     * @throws IOException
     *             if thrown while reading the configuration file
     * @throws ConfigurationException
     *             if the configuration file is invalid
     */
    public static EngineConfiguration loadDirectory(Path configDir)
            throws IOException {
        return load(configDir.resolve(DEFAULT_CONFIG_FILE_NAME).toFile());
    }

    /**
     * Reads the configuration from the given file path.
     *
     * @param configFile
     *            a path to a configuration file.
     * @return the configuration, or <code>null</code> if the configuration file
     *         does not exist
     * @throws IOException
     *             if thrown while reading the configuration file
     * @throws ConfigurationException
     *             if the configuration file is invalid
     */
    public static EngineConfiguration load(File configFile) throws IOException {
        if (!configFile.isFile()) {
            return null;
        }

        try {
            return MAPPER.readValue(configFile, EngineConfiguration.class);
        }
        // This is mainly to wrap Jackson exceptions, but declaring them
        // explicitly can cause problems in tests if they are not on the
        // classpath
        catch (RuntimeException e) {
            throw new ConfigurationException(e);
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
                && Objects.equals(buildDir, that.buildDir)
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
        return Objects.hash(baseDir, classPath, generator, parser, buildDir,
                classesDir, outputDir);
    }

    public void store(File file) throws IOException {
        MAPPER.writeValue(file, this);
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
            this.configuration.buildDir = configuration.buildDir;
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
            configuration.buildDir = resolve(value);
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
