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
            var expectedBare = testHelper.loadExpected("expected/GenericsBareEndpoint.ts");
            assertTypeScriptEquals("GenericsBareEndpoint.ts", bareEndpointTs, expectedBare);

            var bareEntityEndpointTs = generated.get("GenericsBareEntityEndpoint.ts");
            assertNotNull(bareEntityEndpointTs, "GenericsBareEntityEndpoint.ts should be generated");
            var expectedBareEntity = testHelper.loadExpected("expected/GenericsBareEntityEndpoint.ts");
            assertTypeScriptEquals("GenericsBareEntityEndpoint.ts", bareEntityEndpointTs, expectedBareEntity);

            var extendedEndpointTs = generated.get("GenericsExtendedEndpoint.ts");
            assertNotNull(extendedEndpointTs, "GenericsExtendedEndpoint.ts should be generated");
            var expectedExtended = testHelper.loadExpected("expected/GenericsExtendedEndpoint.ts");
            assertTypeScriptEquals("GenericsExtendedEndpoint.ts", extendedEndpointTs, expectedExtended);

            var methodsEndpointTs = generated.get("GenericsMethodsEndpoint.ts");
            assertNotNull(methodsEndpointTs, "GenericsMethodsEndpoint.ts should be generated");
            var expectedMethods = testHelper.loadExpected("expected/GenericsMethodsEndpoint.ts");
            assertTypeScriptEquals("GenericsMethodsEndpoint.ts", methodsEndpointTs, expectedMethods);

            var refEndpointTs = generated.get("GenericsRefEndpoint.ts");
            assertNotNull(refEndpointTs, "GenericsRefEndpoint.ts should be generated");
            var expectedRef = testHelper.loadExpected("expected/GenericsRefEndpoint.ts");
            assertTypeScriptEquals("GenericsRefEndpoint.ts", refEndpointTs, expectedRef);

            var implementInterfaceEndpointTs = generated.get("ImplementInterfaceEndpoint.ts");
            assertNotNull(implementInterfaceEndpointTs, "ImplementInterfaceEndpoint.ts should be generated");
            var expectedImplementInterface = testHelper.loadExpected("expected/ImplementInterfaceEndpoint.ts");
            assertTypeScriptEquals("ImplementInterfaceEndpoint.ts", implementInterfaceEndpointTs, expectedImplementInterface);
        } finally {
            testHelper.cleanup();
        }
    }
}
