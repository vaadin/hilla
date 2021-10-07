package com.vaadin.fusion.parser.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.servers.Server;

public class ParserConfigTests {
    @Test
    public void should_parseJSONConfigFile() throws URISyntaxException {
        ParserConfig config = new ParserConfig.Factory(
                findResource("json-test.config.json")).finish();

        assertConfig(config);
    }

    @Test
    public void should_parseYAMLConfigFile() throws URISyntaxException {
        ParserConfig config = new ParserConfig.Factory(
                findResource("yaml-test.config.yml")).finish();

        assertConfig(config);
    }

    @Test
    public void should_parseAlmostEmptyConfigFile() throws URISyntaxException {
        ParserConfig config = new ParserConfig.Factory(
                findResource("absent-elements.config.json")).finish();

        assertEquals("/path/to/bytecode", config.getClassPath().get());
        assertEquals("Vaadin Application", config.getApplication().getName());
        assertEquals("1.0.0-SNAPSHOT", config.getApplication().getVersion());
        assertEquals("com.vaadin.fusion.Endpoint",
                config.getApplication().getEndpointAnnotation());
        assertEquals(Collections.emptyList(),
                config.getApplication().getServers());
        assertEquals(Collections.emptySet(), config.getPlugins().getDisable());
        assertEquals(Collections.emptySet(), config.getPlugins().getUse());
    }

    private void assertConfig(ParserConfig config) {
        assertFalse(config.getClassPath().isPresent());
        assertEquals("com.vaadin.fusion.Endpoint",
                config.getApplication().getEndpointAnnotation());
        assertEquals(Collections.singleton("generate-openapi"),
                config.getPlugins().getDisable());
        assertEquals(
                Collections.singletonList(
                        new Server().description("Vaadin backend")
                                .url("http://localhost:8080/connect")),
                config.getApplication().getServers());
        assertEquals(
                new LinkedHashSet<>(
                        Arrays.asList("com.vaadin.fusion.parser.BasicPlugin",
                                "com.vaadin.fusion.parser.DependencyPlugin")),
                config.getPlugins().getUse());
    }

    private File findResource(String resourceName) throws URISyntaxException {
        return Paths.get(Objects
                .requireNonNull(getClass().getResource(resourceName)).toURI())
                .toFile();
    }
}
