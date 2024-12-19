package com.vaadin.hilla.parser.plugins.backbone.multiendpoints;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;

public class MultiEndpointsTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_UseAppropriateSchema_When_SimpleTypesAreUsed()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(MultiEndpointsBarEndpoint.class,
                        MultiEndpointsBazEndpoint.class,
                        MultiEndpointsFooEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }
}
