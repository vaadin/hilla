package com.vaadin.hilla.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;

import com.vaadin.hilla.parser.plugins.nonnull.AnnotationMatcher;
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPluginConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EngineConfigurationTest {
    private static final String CONFIG_FILE_NAME = "hilla-engine-configuration.json";
    private Path baseDirectory;
    private File configFile;
    private EngineConfiguration.Builder configurationBuilder;
    private Path temporaryDirectory;

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Endpoint {
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface EndpointExposed {
    }

    @BeforeEach
    public void setUp() throws IOException {
        this.temporaryDirectory = Files
                .createTempDirectory(getClass().getName());
        this.configFile = this.temporaryDirectory.resolve(CONFIG_FILE_NAME)
                .toFile();

        this.baseDirectory = this.temporaryDirectory.resolve("base");

        var parserConfiguration = new ParserConfiguration();
        parserConfiguration.setEndpointAnnotations(List.of(Endpoint.class));
        parserConfiguration
                .setEndpointExposedAnnotations(List.of(EndpointExposed.class));
        parserConfiguration.setPlugins(new ParserConfiguration.Plugins(List.of(
                new ParserConfiguration.Plugin("parser-jvm-plugin-use"),
                new ParserConfiguration.Plugin("parser-jvm-plugin-nonnull",
                        new NonnullPluginConfig(List.of(new AnnotationMatcher(
                                "com.example.application.annotations.NeverNull",
                                false, 50)), List.of()))),
                List.of(new ParserConfiguration.Plugin(
                        "parser-jvm-plugin-disable")),
                true));
        parserConfiguration.setOpenAPIBasePath("test-openapi.json");

        var generatorConfiguration = new GeneratorConfiguration();
        generatorConfiguration.setPlugins(new GeneratorConfiguration.Plugins(
                List.of(new GeneratorConfiguration.Plugin(
                        "generator-jvm-plugin" + "-use")),
                List.of(new GeneratorConfiguration.Plugin(
                        "generator-jvm-plugin" + "-disable")),
                true));

        this.configurationBuilder = new EngineConfiguration.Builder(
                this.baseDirectory).buildDir("build")
                .outputDir("src/frontend/typescript/generated")
                .classPath(new LinkedHashSet<>(
                        List.of("build/classes", "dependency")))
                .parser(parserConfiguration).generator(generatorConfiguration);
    }

    @Test
    public void should_NotRelativizeOutputDir_WhenGivenRelative() {
        var config = configurationBuilder.outputDir("relative/path").create();
        assertEquals(baseDirectory.resolve("relative/path"),
                config.getOutputDir());
    }

    @Test
    public void should_RelativizeOutputDir_WhenGivenAbsolutePath() {
        var path = temporaryDirectory.resolve("absolute/path");
        var config = configurationBuilder.outputDir(path).create();

        assertEquals(path, config.getOutputDir());
    }

    @Test
    @AfterEach
    public void tearDown() throws IOException {
        if (this.configFile.exists()) {
            Files.delete(this.configFile.toPath());
        }
        Files.delete(this.temporaryDirectory);
    }
}
