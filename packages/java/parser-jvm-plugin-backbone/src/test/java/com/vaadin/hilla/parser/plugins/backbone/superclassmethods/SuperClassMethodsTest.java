package com.vaadin.hilla.parser.plugins.backbone.superclassmethods;

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

public class SuperClassMethodsTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_CorrectlyHandleSuperClassMethods()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(PersonEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_SuperClassMethods() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(PersonEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .withEndpointExposedAnnotations(EndpointExposed.class)
                    .generate();

            var endpointTs = generated.get("PersonEndpoint.ts");
            assertNotNull(endpointTs, "PersonEndpoint.ts should be generated");

            var expectedEndpoint = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type Person_1 from "./com/vaadin/hilla/parser/plugins/backbone/superclassmethods/PersonEndpoint/Person.js";
                import client_1 from "./connect-client.default.js";
                async function get_1(id: number | undefined, init?: EndpointRequestInit_1): Promise<Person_1 | undefined> { return client_1.call("PersonEndpoint", "get", { id }, init); }
                async function delete_1(id: number | undefined, init?: EndpointRequestInit_1): Promise<void> { return client_1.call("PersonEndpoint", "delete", { id }, init); }
                async function update_1(entity: Person_1 | undefined, init?: EndpointRequestInit_1): Promise<Person_1 | undefined> { return client_1.call("PersonEndpoint", "update", { entity }, init); }
                async function getNonNullablePage_1(pageSize: number, pageNumber: number, parameters: Record<string, Person_1 | undefined> | undefined, init?: EndpointRequestInit_1): Promise<Array<Person_1 | undefined> | undefined> { return client_1.call("PersonEndpoint", "getNonNullablePage", { pageSize, pageNumber, parameters }, init); }
                async function getPage_1(pageSize: number, pageNumber: number, init?: EndpointRequestInit_1): Promise<Array<Person_1 | undefined> | undefined> { return client_1.call("PersonEndpoint", "getPage", { pageSize, pageNumber }, init); }
                async function size_1(init?: EndpointRequestInit_1): Promise<number> { return client_1.call("PersonEndpoint", "size", {}, init); }
                export { delete_1 as delete, get_1 as get, getNonNullablePage_1 as getNonNullablePage, getPage_1 as getPage, size_1 as size, update_1 as update };
                """;
            assertTypeScriptEquals("PersonEndpoint.ts", endpointTs, expectedEndpoint);
        } finally {
            testHelper.cleanup();
        }
    }
}
