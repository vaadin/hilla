package com.vaadin.hilla.parser.plugins.backbone.generics;

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

public class GenericsTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_ParseGenericTypes()
            throws IOException, URISyntaxException {
        var openAPI = new Parser().classLoader(getClass().getClassLoader())
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(GenericsBareEndpoint.class,
                        GenericsBareEntityEndpoint.class,
                        GenericsExtendedEndpoint.class,
                        GenericsMethodsEndpoint.class,
                        GenericsRefEndpoint.class,
                        ImplementInterfaceEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }
}
