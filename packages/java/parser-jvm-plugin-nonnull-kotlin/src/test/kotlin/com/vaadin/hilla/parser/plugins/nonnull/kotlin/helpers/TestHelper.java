package com.vaadin.hilla.parser.plugins.nonnull.kotlin.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.hilla.parser.testutils.ResourceLoader;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static com.vaadin.hilla.parser.testutils.JsonAssertions.assertEquals;

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

    public void executeParserWithConfig(OpenAPI openAPI)
            throws Exception {
        var expected = mapper.readValue(resourceLoader.find("openapi.json"),
                OpenAPI.class);

        assertEquals(expected, openAPI);
    }

    public Path getTargetDir() {
        return targetDir;
    }
}
