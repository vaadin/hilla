package com.vaadin.hilla.parser.plugins.backbone.generics;

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

public class GenericsTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_ParseGenericTypes()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(GenericsBareEndpoint.class,
                        GenericsBareEntityEndpoint.class,
                        GenericsExtendedEndpoint.class,
                        GenericsMethodsEndpoint.class,
                        GenericsRefEndpoint.class,
                        ImplementInterfaceEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_GenericTypes() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(GenericsBareEndpoint.class,
                            GenericsBareEntityEndpoint.class,
                            GenericsExtendedEndpoint.class,
                            GenericsMethodsEndpoint.class,
                            GenericsRefEndpoint.class,
                            ImplementInterfaceEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .withEndpointExposedAnnotations(EndpointExposed.class)
                    .generate();

            // Verify each endpoint
            var bareEndpointTs = generated.get("GenericsBareEndpoint.ts");
            assertNotNull(bareEndpointTs, "GenericsBareEndpoint.ts should be generated");
            var expectedBare = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import client_1 from "./connect-client.default.js";
                async function getSomething_1(something: unknown, init?: EndpointRequestInit_1): Promise<unknown> { return client_1.call("GenericsBareEndpoint", "getSomething", { something }, init); }
                export { getSomething_1 as getSomething };
                """;
            assertTypeScriptEquals("GenericsBareEndpoint.ts", bareEndpointTs, expectedBare);

            var bareEntityEndpointTs = generated.get("GenericsBareEntityEndpoint.ts");
            assertNotNull(bareEntityEndpointTs, "GenericsBareEntityEndpoint.ts should be generated");
            var expectedBareEntity = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type GenericsBareEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsBareEntityEndpoint/GenericsBareEntity.js";
                import type GenericsRecord_1 from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsBareEntityEndpoint/GenericsRecord.js";
                import type GenericsBareRefEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsBareRefEntity.js";
                import client_1 from "./connect-client.default.js";
                async function getBareEntity_1(ref: GenericsBareRefEntity_1<GenericsBareEntity_1 | undefined> | undefined, init?: EndpointRequestInit_1): Promise<GenericsBareRefEntity_1<GenericsBareEntity_1 | undefined> | undefined> { return client_1.call("GenericsBareEntityEndpoint", "getBareEntity", { ref }, init); }
                async function getBareEntityList_1(ref: GenericsBareRefEntity_1<Array<number | undefined> | undefined> | undefined, init?: EndpointRequestInit_1): Promise<GenericsBareRefEntity_1<Array<number | undefined> | undefined> | undefined> { return client_1.call("GenericsBareEntityEndpoint", "getBareEntityList", { ref }, init); }
                async function getBareReference_1(ref: GenericsBareRefEntity_1<string | undefined> | undefined, init?: EndpointRequestInit_1): Promise<GenericsBareRefEntity_1<string | undefined> | undefined> { return client_1.call("GenericsBareEntityEndpoint", "getBareReference", { ref }, init); }
                async function getRecord_1(record: GenericsRecord_1<string | undefined, string | undefined> | undefined, init?: EndpointRequestInit_1): Promise<GenericsRecord_1<string | undefined, string | undefined> | undefined> { return client_1.call("GenericsBareEntityEndpoint", "getRecord", { record }, init); }
                export { getBareEntity_1 as getBareEntity, getBareEntityList_1 as getBareEntityList, getBareReference_1 as getBareReference, getRecord_1 as getRecord };
                """;
            assertTypeScriptEquals("GenericsBareEntityEndpoint.ts", bareEntityEndpointTs, expectedBareEntity);

            var extendedEndpointTs = generated.get("GenericsExtendedEndpoint.ts");
            assertNotNull(extendedEndpointTs, "GenericsExtendedEndpoint.ts should be generated");
            var expectedExtended = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import client_1 from "./connect-client.default.js";
                async function getMap_1(map: Record<string, unknown> | undefined, init?: EndpointRequestInit_1): Promise<Record<string, unknown> | undefined> { return client_1.call("GenericsExtendedEndpoint", "getMap", { map }, init); }
                export { getMap_1 as getMap };
                """;
            assertTypeScriptEquals("GenericsExtendedEndpoint.ts", extendedEndpointTs, expectedExtended);

            var methodsEndpointTs = generated.get("GenericsMethodsEndpoint.ts");
            assertNotNull(methodsEndpointTs, "GenericsMethodsEndpoint.ts should be generated");
            var expectedMethods = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type GenericsBareRefEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsBareRefEntity.js";
                import client_1 from "./connect-client.default.js";
                async function getList_1(list: Array<string | undefined> | undefined, init?: EndpointRequestInit_1): Promise<Array<string | undefined> | undefined> { return client_1.call("GenericsMethodsEndpoint", "getList", { list }, init); }
                async function getRef_1(ref: GenericsBareRefEntity_1<string | undefined> | undefined, init?: EndpointRequestInit_1): Promise<GenericsBareRefEntity_1<string | undefined> | undefined> { return client_1.call("GenericsMethodsEndpoint", "getRef", { ref }, init); }
                async function getValueWithGenericType_1(something: unknown, init?: EndpointRequestInit_1): Promise<unknown> { return client_1.call("GenericsMethodsEndpoint", "getValueWithGenericType", { something }, init); }
                export { getList_1 as getList, getRef_1 as getRef, getValueWithGenericType_1 as getValueWithGenericType };
                """;
            assertTypeScriptEquals("GenericsMethodsEndpoint.ts", methodsEndpointTs, expectedMethods);

            var refEndpointTs = generated.get("GenericsRefEndpoint.ts");
            assertNotNull(refEndpointTs, "GenericsRefEndpoint.ts should be generated");
            var expectedRef = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type GenericsBareRefEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsBareRefEntity.js";
                import type GenericsExtendedRefEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsExtendedRefEntity.js";
                import client_1 from "./connect-client.default.js";
                async function getBareReference_1(ref: GenericsBareRefEntity_1<string | undefined> | undefined, init?: EndpointRequestInit_1): Promise<GenericsBareRefEntity_1<string | undefined> | undefined> { return client_1.call("GenericsRefEndpoint", "getBareReference", { ref }, init); }
                async function getExtendedReference_1(ref: GenericsExtendedRefEntity_1<GenericsBareRefEntity_1<string | undefined> | undefined> | undefined, init?: EndpointRequestInit_1): Promise<GenericsExtendedRefEntity_1<GenericsBareRefEntity_1<string | undefined> | undefined> | undefined> { return client_1.call("GenericsRefEndpoint", "getExtendedReference", { ref }, init); }
                export { getBareReference_1 as getBareReference, getExtendedReference_1 as getExtendedReference };
                """;
            assertTypeScriptEquals("GenericsRefEndpoint.ts", refEndpointTs, expectedRef);

            var implementInterfaceEndpointTs = generated.get("ImplementInterfaceEndpoint.ts");
            assertNotNull(implementInterfaceEndpointTs, "ImplementInterfaceEndpoint.ts should be generated");
            var expectedImplementInterface = """
                import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
                import type ConcreteType_1 from "./com/vaadin/hilla/parser/plugins/backbone/generics/ConcreteType.js";
                import client_1 from "./connect-client.default.js";
                async function dealWithConcreteType_1(object: ConcreteType_1 | undefined, init?: EndpointRequestInit_1): Promise<ConcreteType_1 | undefined> { return client_1.call("ImplementInterfaceEndpoint", "dealWithConcreteType", { object }, init); }
                async function dealWithGenericType_1(object: ConcreteType_1 | undefined, init?: EndpointRequestInit_1): Promise<ConcreteType_1 | undefined> { return client_1.call("ImplementInterfaceEndpoint", "dealWithGenericType", { object }, init); }
                async function dealWithItAgain_1(object: ConcreteType_1 | undefined, init?: EndpointRequestInit_1): Promise<ConcreteType_1 | undefined> { return client_1.call("ImplementInterfaceEndpoint", "dealWithItAgain", { object }, init); }
                export { dealWithConcreteType_1 as dealWithConcreteType, dealWithGenericType_1 as dealWithGenericType, dealWithItAgain_1 as dealWithItAgain };
                """;
            assertTypeScriptEquals("ImplementInterfaceEndpoint.ts", implementInterfaceEndpointTs, expectedImplementInterface);
        } finally {
            testHelper.cleanup();
        }
    }
}
