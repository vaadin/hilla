package com.vaadin.hilla.parser.plugins.backbone.shadowedname;

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

public class ShadowedNameTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_DistinguishBetweenUserAndBuiltinTypes_When_TheyHaveSameName()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(ShadowedNameEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_ShadowedNames() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(ShadowedNameEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var endpointTs = generated.get("ShadowedNameEndpoint.ts");
            assertNotNull(endpointTs, "ShadowedNameEndpoint.ts should be generated");

            var expectedEndpoint = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type Collection_1 from "./com/vaadin/hilla/parser/plugins/backbone/shadowedname/ShadowedNameEndpoint/Collection.js";
                import type Collection_2 from "./com/vaadin/hilla/parser/plugins/backbone/shadowedname/subpackage/Collection.js";
                import client_1 from "./connect-client.default.js";
                async function getJavaCollection_1(init?: EndpointRequestInit_1): Promise<Array<string | undefined> | undefined> { return client_1.call("ShadowedNameEndpoint", "getJavaCollection", {}, init); }
                async function getNestedUserDefinedCollection_1(name: string | undefined, init?: EndpointRequestInit_1): Promise<Collection_1 | undefined> { return client_1.call("ShadowedNameEndpoint", "getNestedUserDefinedCollection", { name }, init); }
                async function getSeparateUserDefinedCollection_1(init?: EndpointRequestInit_1): Promise<Collection_2<string | undefined> | undefined> { return client_1.call("ShadowedNameEndpoint", "getSeparateUserDefinedCollection", {}, init); }
                export { getJavaCollection_1 as getJavaCollection, getNestedUserDefinedCollection_1 as getNestedUserDefinedCollection, getSeparateUserDefinedCollection_1 as getSeparateUserDefinedCollection };
                """;
            assertTypeScriptEquals("ShadowedNameEndpoint.ts", endpointTs, expectedEndpoint);
        } finally {
            testHelper.cleanup();
        }
    }
}
