package com.vaadin.hilla.parser.plugins.backbone.genericsuperclassmethods;

import static com.vaadin.hilla.parser.testutils.TypeScriptAssertions.assertTypeScriptEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;
import com.vaadin.hilla.parser.testutils.EndToEndTestHelper;
import org.junit.jupiter.api.Test;

public class GenericSuperClassMethodsTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_CorrectlyHandleSuperClassMethods()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(GenericSuperClassLong.class,
                        GenericSuperClassString.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_GenericSuperClassMethods() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(GenericSuperClassLong.class,
                            GenericSuperClassString.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .withEndpointExposedAnnotations(EndpointExposed.class)
                    .generate();

            var longEndpointTs = generated.get("GenericSuperClassLong.ts");
            assertNotNull(longEndpointTs, "GenericSuperClassLong.ts should be generated");
            var expectedLong = testHelper.loadExpected("expected/GenericSuperClassLong.ts");
            assertTypeScriptEquals("GenericSuperClassLong.ts", longEndpointTs, expectedLong);

            var stringEndpointTs = generated.get("GenericSuperClassString.ts");
            assertNotNull(stringEndpointTs, "GenericSuperClassString.ts should be generated");
            var expectedString = testHelper.loadExpected("expected/GenericSuperClassString.ts");
            assertTypeScriptEquals("GenericSuperClassString.ts", stringEndpointTs, expectedString);
        } finally {
            testHelper.cleanup();
        }
    }
}
