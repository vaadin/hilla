package com.vaadin.hilla.parser.plugins.transfertypes.uuid;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.TransferTypesPlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.test.helpers.TestHelper;

public class UUIDTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_ReplaceUUIDClassWithString()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin())
                .addPlugin(new TransferTypesPlugin())
                .execute(List.of(UUIDEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }
}
