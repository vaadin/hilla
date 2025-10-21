package com.vaadin.hilla.parser.plugins.backbone.simpletype;

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

public class SimpleTypeTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_UseAppropriateSchema_When_SimpleTypesAreUsed()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(SimpleTypeEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_When_SimpleTypesAreUsed() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            // Generate TypeScript from Java (full pipeline)
            var generated = testHelper
                    .withEndpoints(SimpleTypeEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            // Assert endpoint file was generated
            var endpointTs = generated.get("SimpleTypeEndpoint.ts");
            assertNotNull(endpointTs, "SimpleTypeEndpoint.ts should be generated");

            // Assert it matches expected output
            var expectedEndpoint = testHelper.loadExpected("expected/SimpleTypeEndpoint.ts");
            assertTypeScriptEquals("SimpleTypeEndpoint.ts", endpointTs, expectedEndpoint);
        } finally {
            testHelper.cleanup();
        }
    }
}
