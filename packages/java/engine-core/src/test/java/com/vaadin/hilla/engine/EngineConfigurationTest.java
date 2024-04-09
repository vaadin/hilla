package com.vaadin.hilla.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;

import com.vaadin.hilla.parser.plugins.nonnull.AnnotationMatcher;
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPluginConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.testutils.JsonAssertions;
import com.vaadin.hilla.parser.testutils.TestEngineConfigurationPathResolver;

public class EngineConfigurationTest {
    private static final String CONFIG_FILE_NAME = "hilla-engine-configuration.json";
    private static final URL TEST_CONFIG_URL = EngineConfigurationTest.class
            .getResource(CONFIG_FILE_NAME);
    private Path baseDirectory;
    private File configFile;
    private EngineConfiguration.Builder configurationBuilder;
    private Path temporaryDirectory;

    @BeforeEach
    public void setUp() throws IOException {
        this.temporaryDirectory = Files
                .createTempDirectory(getClass().getName());
        this.configFile = this.temporaryDirectory.resolve(CONFIG_FILE_NAME)
                .toFile();

        this.baseDirectory = this.temporaryDirectory.resolve("base");

        var parserConfiguration = new ParserConfiguration();
        parserConfiguration
                .setEndpointAnnotation("com.vaadin.hilla.test.Endpoint");
        parserConfiguration.setEndpointExposedAnnotation(
                "com.vaadin.hilla.test.EndpointExposed");
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
    public void should_DeserializeFromJsonFile()
            throws IOException, URISyntaxException, InvocationTargetException,
            NoSuchMethodException, InstantiationException,
            IllegalAccessException, NoSuchFieldException {
        var config = configurationBuilder.create();
        Files.copy(Path.of(TEST_CONFIG_URL.toURI()), configFile.toPath());

        var loadedConfig = TestEngineConfigurationPathResolver
                .resolve(EngineConfiguration.load(configFile), baseDirectory);

        assertNotNull(loadedConfig);
        JsonAssertions.assertEquals(config, loadedConfig);
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
    public void should_SerializeToJsonFile() throws IOException,
            InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        configurationBuilder.create().store(configFile);

        var storedConfig = EngineConfiguration.MAPPER.readValue(configFile,
                EngineConfiguration.class);
        var expectedConfig = TestEngineConfigurationPathResolver
                .resolve(EngineConfiguration.MAPPER.readValue(TEST_CONFIG_URL,
                        EngineConfiguration.class), baseDirectory);

        JsonAssertions.assertEquals(expectedConfig, storedConfig);
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (this.configFile.exists()) {
            Files.delete(this.configFile.toPath());
        }
        Files.delete(this.temporaryDirectory);
    }
}
