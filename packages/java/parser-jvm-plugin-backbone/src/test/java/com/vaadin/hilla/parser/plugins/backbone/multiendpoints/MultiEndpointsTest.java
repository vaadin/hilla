package com.vaadin.hilla.parser.plugins.backbone.multiendpoints;

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

public class MultiEndpointsTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_UseAppropriateSchema_When_SimpleTypesAreUsed()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(MultiEndpointsBarEndpoint.class,
                        MultiEndpointsBazEndpoint.class,
                        MultiEndpointsFooEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_MultipleEndpoints() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(MultiEndpointsBarEndpoint.class,
                            MultiEndpointsBazEndpoint.class,
                            MultiEndpointsFooEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var barEndpointTs = generated.get("MultiEndpointsBarEndpoint.ts");
            assertNotNull(barEndpointTs, "MultiEndpointsBarEndpoint.ts should be generated");
            var expectedBar = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type MultiEndpointsSharedModel_1 from "./com/vaadin/hilla/parser/plugins/backbone/multiendpoints/MultiEndpointsSharedModel.js";
                import client_1 from "./connect-client.default.js";
                async function getBar_1(init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("MultiEndpointsBarEndpoint", "getBar", {}, init); }
                async function getShared_1(init?: EndpointRequestInit_1): Promise<MultiEndpointsSharedModel_1 | undefined> { return client_1.call("MultiEndpointsBarEndpoint", "getShared", {}, init); }
                export { getBar_1 as getBar, getShared_1 as getShared };
                """;
            assertTypeScriptEquals("MultiEndpointsBarEndpoint.ts", barEndpointTs, expectedBar);

            var bazEndpointTs = generated.get("MultiEndpointsBazEndpoint.ts");
            assertNotNull(bazEndpointTs, "MultiEndpointsBazEndpoint.ts should be generated");
            var expectedBaz = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type MultiEndpointsSharedModel_1 from "./com/vaadin/hilla/parser/plugins/backbone/multiendpoints/MultiEndpointsSharedModel.js";
                import client_1 from "./connect-client.default.js";
                async function getBaz_1(init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("MultiEndpointsBazEndpoint", "getBaz", {}, init); }
                async function getShared_1(init?: EndpointRequestInit_1): Promise<MultiEndpointsSharedModel_1 | undefined> { return client_1.call("MultiEndpointsBazEndpoint", "getShared", {}, init); }
                export { getBaz_1 as getBaz, getShared_1 as getShared };
                """;
            assertTypeScriptEquals("MultiEndpointsBazEndpoint.ts", bazEndpointTs, expectedBaz);

            var fooEndpointTs = generated.get("MultiEndpointsFooEndpoint.ts");
            assertNotNull(fooEndpointTs, "MultiEndpointsFooEndpoint.ts should be generated");
            var expectedFoo = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type MultiEndpointsSharedModel_1 from "./com/vaadin/hilla/parser/plugins/backbone/multiendpoints/MultiEndpointsSharedModel.js";
                import client_1 from "./connect-client.default.js";
                async function getFoo_1(init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("MultiEndpointsFooEndpoint", "getFoo", {}, init); }
                async function getShared_1(init?: EndpointRequestInit_1): Promise<MultiEndpointsSharedModel_1 | undefined> { return client_1.call("MultiEndpointsFooEndpoint", "getShared", {}, init); }
                export { getFoo_1 as getFoo, getShared_1 as getShared };
                """;
            assertTypeScriptEquals("MultiEndpointsFooEndpoint.ts", fooEndpointTs, expectedFoo);
        } finally {
            testHelper.cleanup();
        }
    }
}
