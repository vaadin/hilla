package com.vaadin.hilla.parser.plugins.backbone.exposed;

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

public class ExposedTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_CorrectlyHandleEndpointExposedAnnotation()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(ExposedEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_With_EndpointExposedAnnotation() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(ExposedEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .withEndpointExposedAnnotations(EndpointExposed.class)
                    .generate();

            var endpointTs = generated.get("ExposedEndpoint.ts");
            assertNotNull(endpointTs, "ExposedEndpoint.ts should be generated");

            var expectedEndpoint = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type ExposedInterfaceEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/exposed/ExposedInterfaceEntity.js";
                import type ExposedSuperclassEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/exposed/ExposedSuperclassEntity.js";
                import client_1 from "./connect-client.default.js";
                async function methodFromExposedSuperclass_1(init?: EndpointRequestInit_1): Promise<ExposedSuperclassEntity_1 | undefined> { return client_1.call("ExposedEndpoint", "methodFromExposedSuperclass", {}, init); }
                async function methodFromExposedInterface_1(init?: EndpointRequestInit_1): Promise<ExposedInterfaceEntity_1 | undefined> { return client_1.call("ExposedEndpoint", "methodFromExposedInterface", {}, init); }
                async function methodFromEndpoint_1(init?: EndpointRequestInit_1): Promise<void> { return client_1.call("ExposedEndpoint", "methodFromEndpoint", {}, init); }
                export { methodFromEndpoint_1 as methodFromEndpoint, methodFromExposedInterface_1 as methodFromExposedInterface, methodFromExposedSuperclass_1 as methodFromExposedSuperclass };
                """;
            assertTypeScriptEquals("ExposedEndpoint.ts", endpointTs, expectedEndpoint);
        } finally {
            testHelper.cleanup();
        }
    }
}
