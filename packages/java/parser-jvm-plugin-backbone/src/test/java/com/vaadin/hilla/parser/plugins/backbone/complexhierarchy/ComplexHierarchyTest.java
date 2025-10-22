package com.vaadin.hilla.parser.plugins.backbone.complexhierarchy;

import static com.vaadin.hilla.parser.testutils.TypeScriptAssertions.assertTypeScriptEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import com.vaadin.hilla.parser.plugins.backbone.complexhierarchy.models.ComplexHierarchyGrandParentEndpoint;
import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;
import com.vaadin.hilla.parser.testutils.EndToEndTestHelper;

public class ComplexHierarchyTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_GenerateParentModel_When_UsingChildModel()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(ComplexHierarchyEndpoint.class,
                        ComplexHierarchyGrandParentEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_ComplexHierarchy() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(ComplexHierarchyEndpoint.class,
                            ComplexHierarchyGrandParentEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .withEndpointExposedAnnotations(EndpointExposed.class)
                    .generate();

            var complexEndpointTs = generated.get("ComplexHierarchyEndpoint.ts");
            assertNotNull(complexEndpointTs, "ComplexHierarchyEndpoint.ts should be generated");
            var expectedComplex = testHelper.loadExpected("expected/ComplexHierarchyEndpoint.ts");
            assertTypeScriptEquals("ComplexHierarchyEndpoint.ts", complexEndpointTs, expectedComplex);

            var grandParentEndpointTs = generated.get("ComplexHierarchyGrandParentEndpoint.ts");
            assertNotNull(grandParentEndpointTs, "ComplexHierarchyGrandParentEndpoint.ts should be generated");
            var expectedGrandParent = testHelper.loadExpected("expected/ComplexHierarchyGrandParentEndpoint.ts");
            assertTypeScriptEquals("ComplexHierarchyGrandParentEndpoint.ts", grandParentEndpointTs, expectedGrandParent);
        } finally {
            testHelper.cleanup();
        }
    }
}
