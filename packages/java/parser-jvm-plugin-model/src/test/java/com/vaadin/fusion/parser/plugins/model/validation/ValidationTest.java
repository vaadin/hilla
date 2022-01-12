package com.vaadin.fusion.parser.plugins.model.validation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.fusion.parser.core.ParserConfig;
import com.vaadin.fusion.parser.model.utils.TestBase;
import com.vaadin.fusion.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.fusion.parser.plugins.model.ModelPlugin;

public class ValidationTest extends TestBase {
    @Test
    public void should_GenerateValidations()
            throws IOException, URISyntaxException {
        var config = new ParserConfig.Builder()
                .classPath(Set.of(targetDir.toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new BackbonePlugin()).addPlugin(new ModelPlugin())
                .finish();

        executeParserWithConfig(config);
    }
}
