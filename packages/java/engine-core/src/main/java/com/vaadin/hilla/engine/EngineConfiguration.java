package com.vaadin.hilla.engine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.server.frontend.FrontendUtils;

public class EngineConfiguration {
    private static final EngineConfiguration INSTANCE = new EngineConfiguration();
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

    public EngineConfiguration() {
        baseDir = Path.of(System.getProperty("user.dir"));
        buildDir = baseDir.resolve("target");
        classesDir = buildDir.resolve("classes");
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

    public static EngineConfiguration getDefault() {
        return INSTANCE;
    }

    public Set<Path> getClasspath() {
        return classpath;
    }

    public void setClasspath(Set<Path> classpath) {
        this.classpath = classpath;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public Path getBuildDir() {
        return buildDir;
    }

    public void setBuildDir(Path buildDir) {
        this.buildDir = buildDir;
    }

    public void setBuildDir(String buildDir) {
        this.buildDir = baseDir.resolve(buildDir);
    }

    public Path getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(Path baseDir) {
        this.baseDir = baseDir;
    }

    public Path getClassesDir() {
        return classesDir;
    }

    public void setClassesDir(Path classesDir) {
        this.classesDir = classesDir;
    }

    public GeneratorConfiguration getGenerator() {
        return generator;
    }

    public void setGenerator(GeneratorConfiguration generator) {
        this.generator = generator;
    }

    public Path getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(Path outputDir) {
        this.outputDir = outputDir;
    }

    public ParserConfiguration getParser() {
        return parser;
    }

    public void setParser(ParserConfiguration parser) {
        this.parser = parser;
    }

    public Path getOpenAPIFile(boolean isProductionMode) {
        return isProductionMode ? classesDir.resolve(OPEN_API_PATH)
                : buildDir.resolve(OPEN_API_PATH);
    }
}
