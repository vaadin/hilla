package com.vaadin.hilla.parser.plugins.backbone.hierarchyref;

import static com.vaadin.hilla.parser.testutils.TypeScriptAssertions.assertTypeScriptEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.testutils.EndToEndTestHelper;

public class HierarchyRefTest {

    @Test
    public void should_GenerateCorrectTypeScript_For_HierarchyRef() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(HierarchyRefEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var endpointTs = generated.get("HierarchyRefEndpoint.ts");
            assertNotNull(endpointTs, "HierarchyRefEndpoint.ts should be generated");

            var expectedEndpoint = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type HierarchyRef_1 from "./com/vaadin/hilla/parser/plugins/backbone/hierarchyref/HierarchyRef.js";
                import client_1 from "./connect-client.default.js";
                async function getHierarchyRef_1(data: Array<Record<string, string | undefined> | undefined> | undefined, init?: EndpointRequestInit_1): Promise<HierarchyRef_1 | undefined> { return client_1.call("HierarchyRefEndpoint", "getHierarchyRef", { data }, init); }
                export { getHierarchyRef_1 as getHierarchyRef };
                """;
            assertTypeScriptEquals("HierarchyRefEndpoint.ts", endpointTs, expectedEndpoint);

            var hierarchyRefTs = generated.get("com/vaadin/hilla/parser/plugins/backbone/hierarchyref/HierarchyRef.ts");
            assertNotNull(hierarchyRefTs, "HierarchyRef.ts should be generated");

            var expectedHierarchyRef = """
                import type HierarchyRefSuperclass_1 from "./HierarchyRefSuperclass.js";
                interface HierarchyRef extends HierarchyRefSuperclass_1 {
                    child?: HierarchyRefSuperclass_1;
                }
                export default HierarchyRef;
                """;
            assertTypeScriptEquals("HierarchyRef.ts", hierarchyRefTs, expectedHierarchyRef);

            var superclassTs = generated.get("com/vaadin/hilla/parser/plugins/backbone/hierarchyref/HierarchyRefSuperclass.ts");
            assertNotNull(superclassTs, "HierarchyRefSuperclass.ts should be generated");

            var expectedSuperclass = """
                interface HierarchyRefSuperclass {
                    id: number;
                }
                export default HierarchyRefSuperclass;
                """;
            assertTypeScriptEquals("HierarchyRefSuperclass.ts", superclassTs, expectedSuperclass);
        } finally {
            testHelper.cleanup();
        }
    }
}
