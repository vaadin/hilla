package com.vaadin.hilla.parser.plugins.backbone.jsonvaluenojsoncreator;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;
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
}
