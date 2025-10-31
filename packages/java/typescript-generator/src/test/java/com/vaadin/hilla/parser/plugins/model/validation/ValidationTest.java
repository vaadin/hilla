package com.vaadin.hilla.parser.plugins.model.validation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import com.vaadin.hilla.parser.plugins.model.Endpoint;
import com.vaadin.hilla.parser.plugins.model.EndpointExposed;
import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.model.test.helpers.TestHelper;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.model.ModelPlugin;

public class ValidationTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_GenerateValidations()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin()).addPlugin(new ModelPlugin())
                .execute(List.of(ValidationEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }
}
