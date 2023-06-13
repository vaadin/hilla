package dev.hilla.parser.plugins.nonnull.basic;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.hilla.parser.core.Parser;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.nonnull.AnnotationMatcher;
import dev.hilla.parser.plugins.nonnull.NonnullPlugin;
import dev.hilla.parser.plugins.nonnull.NonnullPluginConfig;
import dev.hilla.parser.plugins.nonnull.test.helpers.TestHelper;

public class BasicTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_ApplyNonNullAnnotation()
            throws IOException, URISyntaxException {
        var plugin = new NonnullPlugin();
        plugin.setConfiguration(new NonnullPluginConfig(Set
                .of(new AnnotationMatcher(Nonnull.class.getName(), false, 0)),
                null));

        var openAPI = new Parser().classLoader(getClass().getClassLoader())
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .endpointExposedAnnotation(EndpointExposed.class.getName())
                .addPlugin(new BackbonePlugin()).addPlugin(plugin).execute();

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
