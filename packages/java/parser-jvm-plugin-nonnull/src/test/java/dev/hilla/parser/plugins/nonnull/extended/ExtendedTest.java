package dev.hilla.parser.plugins.nonnull.extended;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.nonnull.NonnullPlugin;
import dev.hilla.parser.plugins.nonnull.NonnullPluginConfig;
import dev.hilla.parser.plugins.nonnull.utils.TestBase;

public class ExtendedTest extends TestBase {
    @Test
    public void should_ApplyNonNullAnnotation()
            throws IOException, URISyntaxException {
        var plugin = new NonnullPlugin();
        plugin.setConfig(
                new NonnullPluginConfig(Set.of(Nonnull.class.getName()), null));

        var config = new ParserConfig.Builder()
                .classPath(Set.of(targetDir.toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new BackbonePlugin()).addPlugin(plugin).finish();

        executeParserWithConfig(config);
    }
}
