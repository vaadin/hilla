package com.vaadin.hilla.parser.plugins.backbone.jsonvaluenojsoncreator;

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

public class JsonValueNoJsonCreatorTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_notChangeOutcomeAccordingToJsonCreator()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(JsonValueNoJsonCreatorEndpoint.class,
                        JsonCreatorNoJsonValueEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_JsonValueNoJsonCreator() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(JsonValueNoJsonCreatorEndpoint.class,
                            JsonCreatorNoJsonValueEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var jsonValueEndpointTs = generated.get("JsonValueNoJsonCreatorEndpoint.ts");
            assertNotNull(jsonValueEndpointTs, "JsonValueNoJsonCreatorEndpoint.ts should be generated");
            var expectedJsonValue = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import client_1 from "./connect-client.default.js";
                async function getEmail_1(init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("JsonValueNoJsonCreatorEndpoint", "getEmail", {}, init); }
                async function setEmail_1(email: string | undefined, init?: EndpointRequestInit_1): Promise<void> { return client_1.call("JsonValueNoJsonCreatorEndpoint", "setEmail", { email }, init); }
                export { getEmail_1 as getEmail, setEmail_1 as setEmail };
                """;
            assertTypeScriptEquals("JsonValueNoJsonCreatorEndpoint.ts", jsonValueEndpointTs, expectedJsonValue);

            var jsonCreatorEndpointTs = generated.get("JsonCreatorNoJsonValueEndpoint.ts");
            assertNotNull(jsonCreatorEndpointTs, "JsonCreatorNoJsonValueEndpoint.ts should be generated");
            var expectedJsonCreator = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type User_1 from "./com/vaadin/hilla/parser/plugins/backbone/jsonvaluenojsoncreator/JsonCreatorNoJsonValueEndpoint/User.js";
                import client_1 from "./connect-client.default.js";
                async function getUser_1(init?: EndpointRequestInit_1): Promise<User_1 | undefined> { return client_1.call("JsonCreatorNoJsonValueEndpoint", "getUser", {}, init); }
                async function setUser_1(user: User_1 | undefined, init?: EndpointRequestInit_1): Promise<void> { return client_1.call("JsonCreatorNoJsonValueEndpoint", "setUser", { user }, init); }
                export { getUser_1 as getUser, setUser_1 as setUser };
                """;
            assertTypeScriptEquals("JsonCreatorNoJsonValueEndpoint.ts", jsonCreatorEndpointTs, expectedJsonCreator);
        } finally {
            testHelper.cleanup();
        }
    }
}
