package com.vaadin.hilla.parser.plugins.backbone.complextype;

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

public class ComplexTypeTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_CorrectlyHandleComplexTypes()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(ComplexTypeEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_ComplexTypes() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(ComplexTypeEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var endpointTs = generated.get("ComplexTypeEndpoint.ts");
            assertNotNull(endpointTs, "ComplexTypeEndpoint.ts should be generated");

            var expectedEndpoint = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type ComplexTypeModel_1 from "./com/vaadin/hilla/parser/plugins/backbone/complextype/ComplexTypeEndpoint/ComplexTypeModel.js";
                import client_1 from "./connect-client.default.js";
                async function getComplexTypeModel_1(data: Array<Record<string, string | undefined> | undefined> | undefined, init?: EndpointRequestInit_1): Promise<ComplexTypeModel_1 | undefined> { return client_1.call("ComplexTypeEndpoint", "getComplexTypeModel", { data }, init); }
                export { getComplexTypeModel_1 as getComplexTypeModel };
                """;
            assertTypeScriptEquals("ComplexTypeEndpoint.ts", endpointTs, expectedEndpoint);
        } finally {
            testHelper.cleanup();
        }
    }
}
