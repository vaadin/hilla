package com.vaadin.fusion.parser.plugins.nonnull.basic;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.fusion.parser.core.ParserConfig;
import com.vaadin.fusion.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.fusion.parser.plugins.nonnull.NonnullPlugin;
import com.vaadin.fusion.parser.plugins.nonnull.NonnullPluginConfig;
import com.vaadin.fusion.parser.plugins.nonnull.utils.TestBase;

public class BasicTest extends TestBase {
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
