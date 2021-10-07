package com.vaadin.fusion.parser.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import org.junit.jupiter.api.Test;

public class ParserConfigTests {
    @Test
    public void should_parseJSONConfigFile() throws URISyntaxException {
        ParserConfig config = ParserConfig
                .parse(findResource("json-test.config.json"));

        assertConfig(config);
    }

    @Test
    public void should_parseYAMLConfigFile() throws URISyntaxException {
        ParserConfig config = ParserConfig
                .parse(findResource("yaml-test.config.yml"));

        assertConfig(config);
    }

    @Test
    public void should_parseAlmostEmptyConfigFile() throws URISyntaxException {
        ParserConfig config = ParserConfig
            .parse(findResource("absent-elements.config.json"));

        assertEquals("/path/to/bytecode", config.getClassPath().get());
        assertEquals("Vaadin Application", config.getApplication().getName());
        assertEquals("com.vaadin.fusion.Endpoint", config.getApplication().getEndpointAnnotation());
        assertEquals(Collections.emptyList(), config.getPlugins().getDisable());
        assertEquals(Collections.emptyList(), config.getPlugins().getUse());
    }

    private void assertConfig(ParserConfig config) {
        assertFalse(config.getClassPath().isPresent());
        assertEquals("com.vaadin.fusion.Endpoint",
                config.getApplication().getEndpointAnnotation());
        assertEquals(Collections.singletonList("generate-openapi"),
                config.getPlugins().getDisable());
        assertEquals(
                Arrays.asList("com.vaadin.fusion.parser.BasicPlugin",
                        "com.vaadin.fusion.parser.DependencyPlugin"),
                config.getPlugins().getUse());
    }

    private File findResource(String resourceName) throws URISyntaxException {
        return Paths.get(Objects
                .requireNonNull(getClass().getResource(resourceName)).toURI())
                .toFile();
    }
}
