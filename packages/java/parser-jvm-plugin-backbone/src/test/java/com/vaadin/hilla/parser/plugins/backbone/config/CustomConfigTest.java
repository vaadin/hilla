package com.vaadin.hilla.parser.plugins.backbone.config;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePluginConfiguration;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;

public class CustomConfigTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_CorrectlyHandleComplexTypes() throws Exception {
        var pluginConfig = new BackbonePluginConfiguration();
        pluginConfig
                .setObjectMapperFactoryClassName(CustomFactory.class.getName());

        var backbonePlugin = new BackbonePlugin();
        backbonePlugin.setConfiguration(pluginConfig);

        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(backbonePlugin)
                .execute(List.of(CustomConfigEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }
}
