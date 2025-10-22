package com.vaadin.hilla.parser.plugins.backbone.emptyentity;

import static com.vaadin.hilla.parser.testutils.TypeScriptAssertions.assertTypeScriptEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.testutils.EndToEndTestHelper;

public class EmptyEntityTest {

    @Test
    public void should_GenerateCorrectTypeScript_For_EmptyEntity() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(EmptyEntityEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var endpointTs = generated.get("EmptyEntityEndpoint.ts");
            assertNotNull(endpointTs, "EmptyEntityEndpoint.ts should be generated");

            var expectedEndpoint = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type EmptyEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/emptyentity/EmptyEntity.js";
                import client_1 from "./connect-client.default.js";
                async function getEmpty_1(init?: EndpointRequestInit_1): Promise<EmptyEntity_1 | undefined> { return client_1.call("EmptyEntityEndpoint", "getEmpty", {}, init); }
                export { getEmpty_1 as getEmpty };
                """;
            assertTypeScriptEquals("EmptyEntityEndpoint.ts", endpointTs, expectedEndpoint);

            var entityTs = generated.get("com/vaadin/hilla/parser/plugins/backbone/emptyentity/EmptyEntity.ts");
            assertNotNull(entityTs, "EmptyEntity.ts should be generated");

            var expectedEntity = """
                interface EmptyEntity {
                }
                export default EmptyEntity;
                """;
            assertTypeScriptEquals("EmptyEntity.ts", entityTs, expectedEntity);
        } finally {
            testHelper.cleanup();
        }
    }
}
