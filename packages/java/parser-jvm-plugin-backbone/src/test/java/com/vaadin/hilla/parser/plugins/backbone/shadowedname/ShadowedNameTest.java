package com.vaadin.hilla.parser.plugins.backbone.shadowedname;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.EndpointExposed;
import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;

public class ShadowedNameTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_DistinguishBetweenUserAndBuiltinTypes_When_TheyHaveSameName()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(ShadowedNameEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }
}
