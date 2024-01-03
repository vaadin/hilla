package com.vaadin.hilla.parser.plugins.backbone.shadowedname;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;

public class ShadowedNameTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_DistinguishBetweenUserAndBuiltinTypes_When_TheyHaveSameName()
            throws IOException, URISyntaxException {
        var openAPI = new Parser().classLoader(getClass().getClassLoader())
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new BackbonePlugin()).execute();

        helper.executeParserWithConfig(openAPI);
    }
}
