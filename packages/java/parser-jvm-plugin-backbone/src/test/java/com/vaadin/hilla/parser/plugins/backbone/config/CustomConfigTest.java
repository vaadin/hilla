package com.vaadin.hilla.parser.plugins.backbone.config;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.EndpointExposed;
import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePluginConfiguration;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;

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

        var openAPI = new Parser().classLoader(getClass().getClassLoader())
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(backbonePlugin)
                .execute(List.of(CustomConfigEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }
}
