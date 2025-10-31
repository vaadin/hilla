package com.vaadin.hilla.parser.plugins.backbone.complexhierarchy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import com.vaadin.hilla.parser.plugins.backbone.complexhierarchy.models.ComplexHierarchyGrandParentEndpoint;
import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;

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
}
