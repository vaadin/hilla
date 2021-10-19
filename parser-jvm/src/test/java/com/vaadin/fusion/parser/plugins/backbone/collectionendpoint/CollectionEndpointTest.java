package com.vaadin.fusion.parser.plugins.backbone.collectionendpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import io.swagger.v3.core.util.Json;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.vaadin.fusion.parser.core.OpenAPIPrinter;
import com.vaadin.fusion.parser.core.Parser;
import com.vaadin.fusion.parser.core.ParserConfig;
import com.vaadin.fusion.parser.testutils.Endpoint;
import com.vaadin.fusion.parser.testutils.ResourceLoader;

import io.swagger.v3.oas.models.OpenAPI;

public class CollectionEndpointTest {
    private final ResourceLoader resourceLoader = new ResourceLoader(
            getClass());
    private final ObjectMapper mapper = Json.mapper();
    private final OpenAPIPrinter printer = new OpenAPIPrinter();
    private Path targetDir;

    @BeforeEach
    public void setup() throws URISyntaxException {
        targetDir = resourceLoader.findTargetDirPath();
    }

    @Test
    public void should_DistinguishBetweenUserAndBuiltinTypes_When_TheyHaveSameName()
            throws IOException, URISyntaxException {
        ParserConfig config = new ParserConfig.Builder()
                .classPath(targetDir.toString())
                .endpointAnnotation(Endpoint.class.getName())
                .usePlugin(CollectionEndpointPlugin.class.getName()).finish();

        Parser parser = new Parser(config);
        parser.execute();

        OpenAPI expected = mapper.readValue(resourceLoader.find("openapi.json"),
                OpenAPI.class);
        OpenAPI actual = parser.getStorage().getOpenAPI();

        assertEquals(expected, actual);
    }
}
