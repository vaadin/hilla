package com.vaadin.hilla.parser.plugins.backbone.superclassmethods;

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

public class SuperClassMethodsTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_CorrectlyHandleSuperClassMethods()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(PersonEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_SuperClassMethods() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(PersonEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .withEndpointExposedAnnotations(EndpointExposed.class)
                    .generate();

            var endpointTs = generated.get("PersonEndpoint.ts");
            assertNotNull(endpointTs, "PersonEndpoint.ts should be generated");

            var expectedEndpoint = testHelper.loadExpected("expected/PersonEndpoint.ts");
            assertTypeScriptEquals("PersonEndpoint.ts", endpointTs, expectedEndpoint);
        } finally {
            testHelper.cleanup();
        }
    }
}
