package com.vaadin.fusion.parser.core;

import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.fusion.parser.testutils.ResourceLoader;

import static org.junit.jupiter.api.Assertions.assertThrows;


public class ParserConfigTests {
    private final ResourceLoader resourceLoader = new ResourceLoader(
            getClass());
    private Path targetDir;

    @BeforeEach
    public void setup() throws URISyntaxException {
        targetDir = resourceLoader.findTargetDirPath();
    }

    @Test
    public void should_LoadDefaultConfig_When_NoConfigOrOpenAPIFileSet() {
        var config = new ParserConfig.Builder()
                .classPath(targetDir.toString()).finish();
    }

    @Test
    public void should_LoadJSONConfig() throws URISyntaxException {
        testConfigFile("parser-config.json");
    }

    @Test
    public void should_LoadYAMLConfig() throws URISyntaxException {
        testConfigFile("parser-config.yml");
    }

    @Test
    public void should_LoadJSONOpenAPITemplate() throws URISyntaxException {
        testOpenAPITemplate("openapi-template.json");
    }

    @Test
    public void should_LoadYAMLOpenAPITemplate() throws URISyntaxException {
        testOpenAPITemplate("openapi-template.yml");
    }

    @Test
    public void should_ThrowErrorIfNoClassPathSet() {
        assertThrows(NullPointerException.class,
                () -> new ParserConfig.Builder().finish(),
                "Fusion Parser Configuration: Classpath is not provided.");
    }

    private void testConfigFile(String configFileName)
            throws URISyntaxException {
        var config = new ParserConfig.Builder()
                .classPath(targetDir.toString()).finish();
    }

    private void testOpenAPITemplate(String openAPITemplateName)
            throws URISyntaxException {
        var config = new ParserConfig.Builder()
                .classPath(targetDir.toString())
                .finish();

    }
}
