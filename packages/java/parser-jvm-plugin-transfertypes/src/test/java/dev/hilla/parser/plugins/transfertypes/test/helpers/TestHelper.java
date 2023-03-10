package dev.hilla.parser.plugins.transfertypes.test.helpers;

import static dev.hilla.parser.testutils.JsonAssertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.hilla.parser.testutils.ResourceLoader;

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

    public void executeParserWithConfig(OpenAPI openAPI)
            throws IOException, URISyntaxException {
        var expected = mapper.readValue(resourceLoader.find("openapi.json"),
                OpenAPI.class);

        assertEquals(expected, openAPI);
    }

    public String getExtendedClassPath(Class<?>... classes)
            throws URISyntaxException {
        return ResourceLoader.getClasspath(Arrays.stream(classes)
                .map(ResourceLoader::new).collect(Collectors.toList()));
    }

    public Path getTargetDir() {
        return targetDir;
    }
}
