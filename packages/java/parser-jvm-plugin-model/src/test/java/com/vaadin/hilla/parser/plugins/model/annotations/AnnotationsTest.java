package com.vaadin.hilla.parser.plugins.model.annotations;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.model.test.helpers.TestHelper;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.model.Endpoint;
import com.vaadin.hilla.parser.plugins.model.EndpointExposed;
import com.vaadin.hilla.parser.plugins.model.ModelPlugin;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

public class AnnotationsTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_GenerateAnnotations()
            throws IOException, URISyntaxException {
        var openAPI = new Parser().classLoader(getClass().getClassLoader())
                .exposedPackages(Set.of(
                        "com.vaadin.hilla.parser.plugins.model.annotations"))
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .endpointExposedAnnotation(EndpointExposed.class.getName())
                .addPlugin(new BackbonePlugin()).addPlugin(new ModelPlugin())
                .execute();

        helper.executeParserWithConfig(openAPI);
    }
}
