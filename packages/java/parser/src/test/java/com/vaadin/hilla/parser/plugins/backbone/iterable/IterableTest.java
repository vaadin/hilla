package com.vaadin.hilla.parser.plugins.backbone.iterable;

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

public class IterableTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_GenerateStringType_When_ReferringToEnumTypes()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(IterableEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_IterableTypes() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(IterableEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var endpointTs = generated.get("IterableEndpoint.ts");
            assertNotNull(endpointTs, "IterableEndpoint.ts should be generated");

            var expectedEndpoint = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type Foo_1 from "./com/vaadin/hilla/parser/plugins/backbone/iterable/IterableEndpoint/Foo.js";
                import client_1 from "./connect-client.default.js";
                async function getFooAnotherCustomIterable_1(init?: EndpointRequestInit_1): Promise<Array<Foo_1 | undefined> | undefined> { return client_1.call("IterableEndpoint", "getFooAnotherCustomIterable", {}, init); }
                async function getFooArray_1(init?: EndpointRequestInit_1): Promise<Array<Foo_1 | undefined> | undefined> { return client_1.call("IterableEndpoint", "getFooArray", {}, init); }
                async function getFooCustomIterable_1(init?: EndpointRequestInit_1): Promise<Array<Foo_1 | undefined> | undefined> { return client_1.call("IterableEndpoint", "getFooCustomIterable", {}, init); }
                async function getFooIterable_1(init?: EndpointRequestInit_1): Promise<Array<Foo_1 | undefined> | undefined> { return client_1.call("IterableEndpoint", "getFooIterable", {}, init); }
                async function getFooList_1(init?: EndpointRequestInit_1): Promise<Array<Foo_1 | undefined> | undefined> { return client_1.call("IterableEndpoint", "getFooList", {}, init); }
                async function getFooSet_1(init?: EndpointRequestInit_1): Promise<Array<Foo_1 | undefined> | undefined> { return client_1.call("IterableEndpoint", "getFooSet", {}, init); }
                async function getSpecializedIterable_1(init?: EndpointRequestInit_1): Promise<Array<string | undefined> | undefined> { return client_1.call("IterableEndpoint", "getSpecializedIterable", {}, init); }
                async function getSpecializedIterableCustom_1(init?: EndpointRequestInit_1): Promise<Array<Foo_1 | undefined> | undefined> { return client_1.call("IterableEndpoint", "getSpecializedIterableCustom", {}, init); }
                export { getFooAnotherCustomIterable_1 as getFooAnotherCustomIterable, getFooArray_1 as getFooArray, getFooCustomIterable_1 as getFooCustomIterable, getFooIterable_1 as getFooIterable, getFooList_1 as getFooList, getFooSet_1 as getFooSet, getSpecializedIterable_1 as getSpecializedIterable, getSpecializedIterableCustom_1 as getSpecializedIterableCustom };
                """;
            assertTypeScriptEquals("IterableEndpoint.ts", endpointTs, expectedEndpoint);
        } finally {
            testHelper.cleanup();
        }
    }
}
