package com.vaadin.hilla.parser.plugins.backbone.bare;

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

public class BareTypeTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_CorrectlyResolveBareTypes()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(BareTypeEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_BareTypes() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(BareTypeEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var endpointTs = generated.get("BareTypeEndpoint.ts");
            assertNotNull(endpointTs, "BareTypeEndpoint.ts should be generated");

            var expectedEndpoint = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import client_1 from "./connect-client.default.js";
                async function getBareList_1(init?: EndpointRequestInit_1): Promise<Array<unknown> | undefined> { return client_1.call("BareTypeEndpoint", "getBareList", {}, init); }
                async function getBareMap_1(init?: EndpointRequestInit_1): Promise<Record<string, unknown> | undefined> { return client_1.call("BareTypeEndpoint", "getBareMap", {}, init); }
                async function getBareOptional_1(init?: EndpointRequestInit_1): Promise<unknown | undefined> { return client_1.call("BareTypeEndpoint", "getBareOptional", {}, init); }
                export { getBareList_1 as getBareList, getBareMap_1 as getBareMap, getBareOptional_1 as getBareOptional };
                """;
            assertTypeScriptEquals("BareTypeEndpoint.ts", endpointTs, expectedEndpoint);
        } finally {
            testHelper.cleanup();
        }
    }
}
