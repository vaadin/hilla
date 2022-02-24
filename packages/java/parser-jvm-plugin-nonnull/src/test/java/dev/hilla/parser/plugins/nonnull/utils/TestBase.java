package dev.hilla.parser.plugins.nonnull.utils;

import static dev.hilla.parser.testutils.OpenAPIAssertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.hilla.parser.core.Parser;
import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.testutils.ResourceLoader;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

public abstract class TestBase {
    protected final ObjectMapper mapper = Json.mapper();
    protected final ResourceLoader resourceLoader;
    protected Path targetDir;

    {
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
