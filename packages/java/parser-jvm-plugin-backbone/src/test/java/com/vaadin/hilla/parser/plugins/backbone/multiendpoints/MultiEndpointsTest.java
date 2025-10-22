package com.vaadin.hilla.parser.plugins.backbone.multiendpoints;

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

public class MultiEndpointsTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_UseAppropriateSchema_When_SimpleTypesAreUsed()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(MultiEndpointsBarEndpoint.class,
                        MultiEndpointsBazEndpoint.class,
                        MultiEndpointsFooEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_MultipleEndpoints() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(MultiEndpointsBarEndpoint.class,
                            MultiEndpointsBazEndpoint.class,
                            MultiEndpointsFooEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var barEndpointTs = generated.get("MultiEndpointsBarEndpoint.ts");
            assertNotNull(barEndpointTs, "MultiEndpointsBarEndpoint.ts should be generated");
            var expectedBar = testHelper.loadExpected("expected/MultiEndpointsBarEndpoint.ts");
            assertTypeScriptEquals("MultiEndpointsBarEndpoint.ts", barEndpointTs, expectedBar);

            var bazEndpointTs = generated.get("MultiEndpointsBazEndpoint.ts");
            assertNotNull(bazEndpointTs, "MultiEndpointsBazEndpoint.ts should be generated");
            var expectedBaz = testHelper.loadExpected("expected/MultiEndpointsBazEndpoint.ts");
            assertTypeScriptEquals("MultiEndpointsBazEndpoint.ts", bazEndpointTs, expectedBaz);

            var fooEndpointTs = generated.get("MultiEndpointsFooEndpoint.ts");
            assertNotNull(fooEndpointTs, "MultiEndpointsFooEndpoint.ts should be generated");
            var expectedFoo = testHelper.loadExpected("expected/MultiEndpointsFooEndpoint.ts");
            assertTypeScriptEquals("MultiEndpointsFooEndpoint.ts", fooEndpointTs, expectedFoo);
        } finally {
            testHelper.cleanup();
        }
    }
}
