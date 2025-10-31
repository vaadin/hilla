package com.vaadin.hilla.parser.plugins.nonnull.basic;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.nonnull.AnnotationMatcher;
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPlugin;
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPluginConfig;
import com.vaadin.hilla.parser.plugins.nonnull.test.helpers.TestHelper;

public class BasicTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_ApplyNonNullAnnotation()
            throws IOException, URISyntaxException {
        var plugin = new NonnullPlugin();
        plugin.setConfiguration(new NonnullPluginConfig(Set
                .of(new AnnotationMatcher(Nonnull.class.getName(), false, 0)),
                null));

        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin()).addPlugin(plugin)
                .execute(List.of(BasicEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void annotationMatcher_shouldHaveDefaultConstructorAndSetter() {
        // to enable maven initialize instances of AnnotationMatcher from
        // pom.xml configurations, properly, it should have the default
        // constructor and setter methods:
        AnnotationMatcher annotationMatcher = new AnnotationMatcher();
        annotationMatcher.setName("name");
        annotationMatcher.setScore(100);
        annotationMatcher.setMakesNullable(true);
        Assertions.assertEquals("name", annotationMatcher.getName());
        Assertions.assertEquals(100, annotationMatcher.getScore());
        Assertions.assertTrue(annotationMatcher.doesMakeNullable());
    }
}
