package com.vaadin.hilla.parser.plugins.subtypes;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.subtypes.test.helpers.TestHelper;

public class SubTypesMethodsTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_CorrectlyHandleSuperClassMethods()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin()).addPlugin(new SubTypesPlugin())
                .execute(List.of(SubTypesEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }
}
