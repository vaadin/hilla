package com.vaadin.hilla.parser.plugins.backbone.test.helpers;

import static com.vaadin.hilla.parser.testutils.JsonAssertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.vaadin.hilla.parser.testutils.ResourceLoader;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

public final class TestHelper {
    private final ObjectMapper mapper = Json.mapper();
    private final ResourceLoader resourceLoader;
    private final Path targetDir;

    public TestHelper(Class<?> cls) {
        try {
            this.resourceLoader = new ResourceLoader(cls);
            this.targetDir = resourceLoader.findTargetDirPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void executeParserWithConfig(OpenAPI openAPI) {
        try {
            var expected = mapper.readValue(resourceLoader.find("openapi.json"),
                    OpenAPI.class);
            assertEquals(expected, openAPI);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to load expected OpenAPI", e);
        } catch (Exception e) {
            throw new RuntimeException("OpenAPI comparison failed", e);
        }
    }

    public Path getTargetDir() {
        return targetDir;
    }
}
