package com.vaadin.hilla.parser.plugins.backbone.transients;

import static com.vaadin.hilla.parser.testutils.TypeScriptAssertions.assertTypeScriptEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;
import com.vaadin.hilla.parser.testutils.EndToEndTestHelper;

public class TransientTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_CorrectlyHandleTransients()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(TransientEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_Transients() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(TransientEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var endpointTs = generated.get("TransientEndpoint.ts");
            assertNotNull(endpointTs, "TransientEndpoint.ts should be generated");

            var expectedEndpoint = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type TransientModel_1 from "./com/vaadin/hilla/parser/plugins/backbone/transients/TransientEndpoint/TransientModel.js";
                import client_1 from "./connect-client.default.js";
                async function getTransientModel_1(init?: EndpointRequestInit_1): Promise<TransientModel_1 | undefined> { return client_1.call("TransientEndpoint", "getTransientModel", {}, init); }
                export { getTransientModel_1 as getTransientModel };
                """;
            assertTypeScriptEquals("TransientEndpoint.ts", endpointTs, expectedEndpoint);
        } finally {
            testHelper.cleanup();
        }
    }
}
