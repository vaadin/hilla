package com.vaadin.fusion.parser.plugins.backbone.shadowedname;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.fusion.parser.core.Parser;
import com.vaadin.fusion.parser.core.ParserConfig;
import com.vaadin.fusion.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.fusion.parser.plugins.testutils.ResourceLoader;

import static com.vaadin.fusion.parser.plugins.testutils.OpenAPIAssertions.assertEquals;

public class ShadowedNameTest {
    private final ResourceLoader resourceLoader = new ResourceLoader(
        getClass());
    private final ObjectMapper mapper = Json.mapper();
    private Path targetDir;

    @BeforeEach
    public void setup() throws URISyntaxException {
        targetDir = resourceLoader.findTargetDirPath();
    }

    @Test
    public void should_DistinguishBetweenUserAndBuiltinTypes_When_TheyHaveSameName()
        throws IOException, URISyntaxException {
        var config = new ParserConfig.Builder()
            .classPath(Set.of(targetDir.toString()))
            .endpointAnnotation(Endpoint.class.getName())
            .addPlugin(BackbonePlugin.class.getName()).finish();

        var parser = new Parser(config);
        parser.execute();

        var expected = mapper.readValue(resourceLoader.find("openapi.json"),
            OpenAPI.class);
        var actual = parser.getStorage().getOpenAPI();

        assertEquals(expected, actual);
    }
}
