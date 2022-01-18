package com.vaadin.fusion.parser.model.utils;

import static com.vaadin.fusion.parser.testutils.OpenAPIAssertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.vaadin.fusion.parser.core.Parser;
import com.vaadin.fusion.parser.core.ParserConfig;
import com.vaadin.fusion.parser.testutils.ResourceLoader;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

import org.junit.jupiter.api.BeforeEach;

public abstract class TestBase {
    protected final ObjectMapper mapper = Json.mapper();
    protected ResourceLoader resourceLoader;
    protected Path targetDir;

    @BeforeEach
    public void beforeEach() {
        var target = getClass();
        resourceLoader = new ResourceLoader(target::getResource,
                target::getProtectionDomain);

        try {
            targetDir = resourceLoader.findTargetDirPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    protected void executeParserWithConfig(ParserConfig config)
            throws IOException, URISyntaxException {
        var parser = new Parser(config);
        parser.execute();

        var expected = mapper.readValue(resourceLoader.find("openapi.json"),
                OpenAPI.class);
        var actual = parser.getStorage().getOpenAPI();

        assertEquals(expected, actual);
    }
}
