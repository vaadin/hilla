package com.vaadin.hilla.parser.plugins.backbone.config;

import static com.vaadin.hilla.parser.testutils.TypeScriptAssertions.assertTypeScriptEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePluginConfiguration;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;
import com.vaadin.hilla.parser.testutils.EndToEndTestHelper;

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
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(backbonePlugin)
                .execute(List.of(CustomConfigEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_CustomConfigEndpoint() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(CustomConfigEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var endpointTs = generated.get("CustomConfigEndpoint.ts");
            assertNotNull(endpointTs, "CustomConfigEndpoint.ts should be generated");

            var expectedEndpoint = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type CustomConfigEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/config/CustomConfigEndpoint/CustomConfigEntity.js";
                import client_1 from "./connect-client.default.js";
                async function get_1(init?: EndpointRequestInit_1): Promise<CustomConfigEntity_1 | undefined> { return client_1.call("CustomConfigEndpoint", "get", {}, init); }
                export { get_1 as get };
                """;
            assertTypeScriptEquals("CustomConfigEndpoint.ts", endpointTs, expectedEndpoint);
        } finally {
            testHelper.cleanup();
        }
    }
}
