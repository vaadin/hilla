package dev.hilla.parser.plugins.backbone.config;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.hilla.parser.core.Parser;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.backbone.BackbonePluginConfiguration;
import dev.hilla.parser.plugins.backbone.test.helpers.TestHelper;

public class CustomConfigTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_CorrectlyHandleComplexTypes()
            throws IOException, URISyntaxException {
        var pluginConfig = new BackbonePluginConfiguration();
        pluginConfig
                .setObjectMapperFactoryClassName(CustomFactory.class.getName());

        var backbonePlugin = new BackbonePlugin();
        backbonePlugin.setConfiguration(pluginConfig);

        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(backbonePlugin).execute();

        helper.executeParserWithConfig(openAPI);
    }
}
