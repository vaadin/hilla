package com.vaadin.hilla.parser.plugins.backbone.bare;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import com.vaadin.hilla.parser.plugins.backbone.config.CustomConfigEndpoint;
import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;

public class BareTypeTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_CorrectlyResolveBareTypes()
            throws IOException, URISyntaxException {
        var openAPI = new Parser().classLoader(getClass().getClassLoader())
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(CustomConfigEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }
}
