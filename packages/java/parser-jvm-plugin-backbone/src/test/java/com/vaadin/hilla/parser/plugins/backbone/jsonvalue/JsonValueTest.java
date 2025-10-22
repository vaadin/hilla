package com.vaadin.hilla.parser.plugins.backbone.jsonvalue;

import static com.vaadin.hilla.parser.testutils.TypeScriptAssertions.assertTypeScriptEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;
import com.vaadin.hilla.parser.testutils.EndToEndTestHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

public class JsonValueTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_CorrectlyMapJsonValue()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(JsonValueEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_JsonValue() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(JsonValueEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var endpointTs = generated.get("JsonValueEndpoint.ts");
            assertNotNull(endpointTs, "JsonValueEndpoint.ts should be generated");

            var expectedEndpoint = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type Person_1 from "./com/vaadin/hilla/parser/plugins/backbone/jsonvalue/JsonValueEndpoint/Person.js";
                import client_1 from "./connect-client.default.js";
                async function getEmail_1(init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("JsonValueEndpoint", "getEmail", {}, init); }
                async function getPerson_1(init?: EndpointRequestInit_1): Promise<Person_1 | undefined> { return client_1.call("JsonValueEndpoint", "getPerson", {}, init); }
                async function setEmail_1(email: string | undefined, init?: EndpointRequestInit_1): Promise<void> { return client_1.call("JsonValueEndpoint", "setEmail", { email }, init); }
                async function setPerson_1(person: Person_1 | undefined, init?: EndpointRequestInit_1): Promise<void> { return client_1.call("JsonValueEndpoint", "setPerson", { person }, init); }
                export { getEmail_1 as getEmail, getPerson_1 as getPerson, setEmail_1 as setEmail, setPerson_1 as setPerson };
                """;
            assertTypeScriptEquals("JsonValueEndpoint.ts", endpointTs, expectedEndpoint);
        } finally {
            testHelper.cleanup();
        }
    }
}
