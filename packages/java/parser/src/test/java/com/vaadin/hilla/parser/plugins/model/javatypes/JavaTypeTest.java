package com.vaadin.hilla.parser.plugins.model.javatypes;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.model.test.helpers.TestHelper;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.model.Endpoint;
import com.vaadin.hilla.parser.plugins.model.EndpointExposed;
import com.vaadin.hilla.parser.plugins.model.ModelPlugin;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

public class JavaTypeTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_GenerateAnnotations()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin()).addPlugin(new ModelPlugin())
                .execute(List.of(JavaTypeEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }
}
