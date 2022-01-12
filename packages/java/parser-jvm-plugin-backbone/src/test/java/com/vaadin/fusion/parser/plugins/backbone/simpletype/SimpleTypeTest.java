package com.vaadin.fusion.parser.plugins.backbone.simpletype;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.fusion.parser.core.ParserConfig;
import com.vaadin.fusion.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.fusion.parser.plugins.backbone.utils.TestBase;

public class SimpleTypeTest extends TestBase {
    @Test
    public void should_UseAppropriateSchema_When_SimpleTypesAreUsed()
            throws IOException, URISyntaxException {
        var config = new ParserConfig.Builder()
                .classPath(Set.of(targetDir.toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new BackbonePlugin()).finish();

        executeParserWithConfig(config);
    }
}
