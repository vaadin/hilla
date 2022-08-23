package dev.hilla.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PluginConfiguration {
    public static final String RESOURCE_NAME = "hilla-engine-configuration.json";
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD,
                    JsonAutoDetect.Visibility.ANY);

    private Path baseDir;
    private Set<String> classPath;
    private GeneratorConfiguration generator;
    private ParserConfiguration parser;

    public static PluginConfiguration load(File targetDir) throws IOException {
        File file = new File(targetDir, RESOURCE_NAME);

        if (!file.exists()) {
            return null;
        }

        return MAPPER.readValue(file, PluginConfiguration.class);
    }

    public void store(File targetDir) throws IOException {
        MAPPER.writeValue(new File(targetDir, RESOURCE_NAME), this);
    }

    public Path getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(Path baseDir) {
        this.baseDir = baseDir;
    }

    public Set<String> getClassPath() {
        return classPath;
    }

    public void setClassPath(Set<String> classPath) {
        this.classPath = classPath;
    }

    public GeneratorConfiguration getGenerator() {
        return generator;
    }

    public void setGenerator(GeneratorConfiguration generator) {
        this.generator = generator;
    }

    public ParserConfiguration getParser() {
        return parser;
    }

    public void setParser(ParserConfiguration parser) {
        this.parser = parser;
    }
}
