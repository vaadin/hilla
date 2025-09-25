package com.vaadin.hilla.parser.model.test.helpers;

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

    public void executeParserWithConfig(OpenAPI openAPI) throws Exception {
        var expected = mapper.readValue(resourceLoader.find("openapi.json"),
                OpenAPI.class);
        try {
            assertEquals(expected, openAPI);
        } catch (Exception e) { // assertEquals throws generic Exception
            throw new RuntimeException("OpenAPI comparison failed", e);
        }
    }

    public Path getTargetDir() {
        return targetDir;
    }
}
