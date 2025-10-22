package com.vaadin.hilla.parser.plugins.backbone.customname;

import static com.vaadin.hilla.parser.testutils.TypeScriptAssertions.assertTypeScriptEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;
import com.vaadin.hilla.parser.testutils.EndToEndTestHelper;
import org.junit.jupiter.api.Test;

public class CustomNameTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_UseCustomEndpointNames_WhenGivenInAnnotation()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(CustomExplicitValueEndpoint.class,
                        CustomNameEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_WithCustomEndpointNames() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(CustomExplicitValueEndpoint.class,
                            CustomNameEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var customExplicitTs = generated.get("NameFromAnnotationExplicitValueEndpoint.ts");
            assertNotNull(customExplicitTs, "NameFromAnnotationExplicitValueEndpoint.ts should be generated");
            var expectedCustomExplicit = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import client_1 from "./connect-client.default.js";
                async function doSomething_1(init?: EndpointRequestInit_1): Promise<void> { return client_1.call("NameFromAnnotationExplicitValueEndpoint", "doSomething", {}, init); }
                export { doSomething_1 as doSomething };
                """;
            assertTypeScriptEquals("NameFromAnnotationExplicitValueEndpoint.ts", customExplicitTs, expectedCustomExplicit);

            var customNameTs = generated.get("NameFromAnnotationEndpoint.ts");
            assertNotNull(customNameTs, "NameFromAnnotationEndpoint.ts should be generated");
            var expectedCustomName = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import client_1 from "./connect-client.default.js";
                async function doSomething_1(init?: EndpointRequestInit_1): Promise<void> { return client_1.call("NameFromAnnotationEndpoint", "doSomething", {}, init); }
                export { doSomething_1 as doSomething };
                """;
            assertTypeScriptEquals("NameFromAnnotationEndpoint.ts", customNameTs, expectedCustomName);
        } finally {
            testHelper.cleanup();
        }
    }
}
