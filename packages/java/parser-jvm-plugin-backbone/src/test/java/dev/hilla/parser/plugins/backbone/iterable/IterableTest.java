package dev.hilla.parser.plugins.backbone.iterable;

import static dev.hilla.parser.plugins.testutils.OpenAPIAssertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.hilla.parser.core.Parser;
import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.testutils.ResourceLoader;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

public class IterableTest {
    private final ObjectMapper mapper = Json.mapper();
    private final ResourceLoader resourceLoader = new ResourceLoader(
            getClass());
    private Path targetDir;

    @BeforeEach
    public void setup() throws URISyntaxException {
        targetDir = resourceLoader.findTargetDirPath();
    }

    @Test
    public void should_GenerateStringType_When_ReferringToEnumTypes()
            throws IOException, URISyntaxException {
        var config = new ParserConfig.Builder()
                .classPath(Set.of(targetDir.toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new BackbonePlugin()).finish();

        var parser = new Parser(config);
        parser.execute();

        var expected = mapper.readValue(resourceLoader.find("openapi.json"),
                OpenAPI.class);
        var actual = parser.getStorage().getOpenAPI();

        assertEquals(expected, actual);
    }
}
