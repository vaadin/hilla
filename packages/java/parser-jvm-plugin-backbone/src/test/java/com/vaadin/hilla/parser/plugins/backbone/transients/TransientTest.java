package com.vaadin.hilla.parser.plugins.backbone.transients;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;

public class TransientTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_CorrectlyHandleTransients()
            throws IOException, URISyntaxException {
        var openAPI = new Parser().classLoader(getClass().getClassLoader())
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class.getName()))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(TransientEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }
}
