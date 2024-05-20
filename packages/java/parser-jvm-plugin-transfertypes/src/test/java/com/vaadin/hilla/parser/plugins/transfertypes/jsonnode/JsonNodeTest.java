package com.vaadin.hilla.parser.plugins.transfertypes.jsonnode;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.TransferTypesPlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.test.helpers.TestHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

public class JsonNodeTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_ReplaceJsonNodeClassWithObject()
            throws IOException, URISyntaxException {
        var openAPI = new Parser().classLoader(getClass().getClassLoader())
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new BackbonePlugin())
                .addPlugin(new TransferTypesPlugin()).execute();

        helper.executeParserWithConfig(openAPI);
    }
}
