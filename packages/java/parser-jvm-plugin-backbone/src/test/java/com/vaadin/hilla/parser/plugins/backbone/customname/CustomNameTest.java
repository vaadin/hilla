package com.vaadin.hilla.parser.plugins.backbone.customname;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.EndpointExposed;
import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;
import org.junit.jupiter.api.Test;

public class CustomNameTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_UseCustomEndpointNames_WhenGivenInAnnotation()
            throws IOException, URISyntaxException {
        var openAPI = new Parser().classLoader(getClass().getClassLoader())
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(CustomExplicitValueEndpoint.class,
                        CustomNameEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }
}
