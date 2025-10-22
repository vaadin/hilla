package com.vaadin.hilla.parser.plugins.backbone.wildcard;

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

public class WildcardTypeTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_CorrectlyResolveWildcardTypes()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(WildcardTypeEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_WildcardTypes() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(WildcardTypeEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var endpointTs = generated.get("WildcardTypeEndpoint.ts");
            assertNotNull(endpointTs, "WildcardTypeEndpoint.ts should be generated");

            var expectedEndpoint = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import client_1 from "./connect-client.default.js";
                async function getDefaultWildcard_1(init?: EndpointRequestInit_1): Promise<Record<string, unknown> | undefined> { return client_1.call("WildcardTypeEndpoint", "getDefaultWildcard", {}, init); }
                async function getExtendingWildcard_1(init?: EndpointRequestInit_1): Promise<Array<Record<string, unknown> | undefined> | undefined> { return client_1.call("WildcardTypeEndpoint", "getExtendingWildcard", {}, init); }
                async function getSuperWildcard_1(init?: EndpointRequestInit_1): Promise<Array<unknown> | undefined> { return client_1.call("WildcardTypeEndpoint", "getSuperWildcard", {}, init); }
                export { getDefaultWildcard_1 as getDefaultWildcard, getExtendingWildcard_1 as getExtendingWildcard, getSuperWildcard_1 as getSuperWildcard };
                """;
            assertTypeScriptEquals("WildcardTypeEndpoint.ts", endpointTs, expectedEndpoint);
        } finally {
            testHelper.cleanup();
        }
    }
}
