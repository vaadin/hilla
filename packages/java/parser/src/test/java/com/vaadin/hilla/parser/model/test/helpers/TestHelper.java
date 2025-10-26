package com.vaadin.hilla.parser.model.test.helpers;

import static com.vaadin.hilla.parser.testutils.JsonAssertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.core.util.Json;
import com.vaadin.hilla.parser.testutils.ResourceLoader;

import io.swagger.v3.oas.models.OpenAPI;

public final class TestHelper {
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

    public void executeParserWithConfig(OpenAPI openAPI)
            throws IOException, URISyntaxException {
        var mapper = Json.mapper();
        var expected = mapper.readValue(resourceLoader.find("openapi.json"),
                OpenAPI.class);

        assertEquals(expected, openAPI);
    }

    public Path getTargetDir() {
        return targetDir;
    }
}
