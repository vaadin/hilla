package com.vaadin.hilla.parser.plugins.backbone.jsonvaluenojsoncreator;

import static com.vaadin.hilla.parser.testutils.TypeScriptAssertions.assertTypeScriptEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;
import com.vaadin.hilla.parser.testutils.EndToEndTestHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

public class JsonValueNoJsonCreatorTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_notChangeOutcomeAccordingToJsonCreator()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(JsonValueNoJsonCreatorEndpoint.class,
                        JsonCreatorNoJsonValueEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_GenerateCorrectTypeScript_For_JsonValueNoJsonCreator() throws Exception {
        var testHelper = new EndToEndTestHelper(getClass());

        try {
            var generated = testHelper
                    .withEndpoints(JsonValueNoJsonCreatorEndpoint.class,
                            JsonCreatorNoJsonValueEndpoint.class)
                    .withEndpointAnnotations(Endpoint.class)
                    .generate();

            var jsonValueEndpointTs = generated.get("JsonValueNoJsonCreatorEndpoint.ts");
            assertNotNull(jsonValueEndpointTs, "JsonValueNoJsonCreatorEndpoint.ts should be generated");
            var expectedJsonValue = testHelper.loadExpected("expected/JsonValueNoJsonCreatorEndpoint.ts");
            assertTypeScriptEquals("JsonValueNoJsonCreatorEndpoint.ts", jsonValueEndpointTs, expectedJsonValue);

            var jsonCreatorEndpointTs = generated.get("JsonCreatorNoJsonValueEndpoint.ts");
            assertNotNull(jsonCreatorEndpointTs, "JsonCreatorNoJsonValueEndpoint.ts should be generated");
            var expectedJsonCreator = testHelper.loadExpected("expected/JsonCreatorNoJsonValueEndpoint.ts");
            assertTypeScriptEquals("JsonCreatorNoJsonValueEndpoint.ts", jsonCreatorEndpointTs, expectedJsonCreator);
        } finally {
            testHelper.cleanup();
        }
    }
}
