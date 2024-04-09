package com.vaadin.hilla.parser.plugins.nonnull.nonnullapi;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.nonnull.AnnotationMatcher;
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPlugin;
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPluginConfig;
import com.vaadin.hilla.parser.plugins.nonnull.test.helpers.TestHelper;

public class NonNullApiTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_ApplyNonNullApiAnnotation()
            throws IOException, URISyntaxException {
        var plugin = new NonnullPlugin();
        plugin.setConfiguration(new NonnullPluginConfig(Set.of(
                new AnnotationMatcher(NonNullApi.class.getName(), false, 10),
                new AnnotationMatcher(NullableField.class.getName(), true, 20),
                new AnnotationMatcher(NullableMethod.class.getName(), true, 20),
                new AnnotationMatcher(NullableParameter.class.getName(), true,
                        20),
                new AnnotationMatcher(NullableSignature.class.getName(), true,
                        20)),
                null));

        var openAPI = new Parser().classLoader(getClass().getClassLoader())
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .endpointExposedAnnotation(EndpointExposed.class.getName())
                .addPlugin(new BackbonePlugin()).addPlugin(plugin).execute();

        helper.executeParserWithConfig(openAPI);
    }
}
