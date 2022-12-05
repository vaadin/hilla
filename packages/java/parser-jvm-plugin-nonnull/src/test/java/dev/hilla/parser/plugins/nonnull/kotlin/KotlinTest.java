package dev.hilla.parser.plugins.nonnull.kotlin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.nonnull.AnnotationMatcher;
import dev.hilla.parser.plugins.nonnull.NonnullPlugin;
import dev.hilla.parser.plugins.nonnull.NonnullPluginConfig;
import dev.hilla.parser.plugins.nonnull.test.helpers.TestHelper;
import dev.hilla.parser.test.fixtures.kotlin.NonNullEndpoint;
import org.junit.jupiter.api.Test;

public class KotlinTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_ApplyNonNullAndNullableInKotlin()
            throws IOException, URISyntaxException {
        var plugin = new NonnullPlugin();
        plugin.setConfiguration(new NonnullPluginConfig(Set
                .of(new AnnotationMatcher("dev.hilla.Nonnull", false, 0)),
                null));

        var config = new ParserConfig.Builder().classPath(Set.of(
                new TestHelper(NonNullEndpoint.class).getTargetDir().toString()))
                .endpointAnnotation("dev.hilla.Endpoint")
                .endpointExposedAnnotation("dev.hilla.EndpointExposed")
                .addPlugin(new BackbonePlugin()).addPlugin(plugin).finish();

        helper.executeParserWithConfig(config);
    }
}
