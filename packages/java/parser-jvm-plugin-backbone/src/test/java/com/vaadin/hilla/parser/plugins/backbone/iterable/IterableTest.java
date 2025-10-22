package com.vaadin.hilla.parser.plugins.backbone.iterable;

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

public class IterableTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_GenerateStringType_When_ReferringToEnumTypes()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(IterableEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_IterableTypes() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(IterableEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var endpointTs = generated.get("IterableEndpoint.ts");
            assertNotNull(endpointTs, "IterableEndpoint.ts should be generated");

            var expectedEndpoint = testHelper.loadExpected("expected/IterableEndpoint.ts");
            assertTypeScriptEquals("IterableEndpoint.ts", endpointTs, expectedEndpoint);
        } finally {
            testHelper.cleanup();
        }
    }
}
