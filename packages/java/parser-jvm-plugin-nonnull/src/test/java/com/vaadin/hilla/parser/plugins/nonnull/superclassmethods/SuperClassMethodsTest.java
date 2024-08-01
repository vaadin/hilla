package com.vaadin.hilla.parser.plugins.nonnull.superclassmethods;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.nonnull.AnnotationMatcher;
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPlugin;
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPluginConfig;
import com.vaadin.hilla.parser.plugins.nonnull.test.helpers.TestHelper;

public class SuperClassMethodsTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_CorrectlyHandleSuperClassMethods()
            throws IOException, URISyntaxException {
        var plugin = new NonnullPlugin();
        plugin.setConfiguration(new NonnullPluginConfig(Set
                .of(new AnnotationMatcher(Nonnull.class.getName(), false, 0)),
                null));

        var openAPI = new Parser().classLoader(getClass().getClassLoader())
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin()).addPlugin(plugin)
                .execute(List.of(PersonEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }
}
