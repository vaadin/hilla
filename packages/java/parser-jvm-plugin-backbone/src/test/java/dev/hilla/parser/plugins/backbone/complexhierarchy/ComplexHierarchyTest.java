package dev.hilla.parser.plugins.backbone.complexhierarchy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.backbone.test.helpers.TestHelper;

public class ComplexHierarchyTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_GenerateParentModel_When_UsingChildModel()
            throws IOException, URISyntaxException {
        var config = new ParserConfig.Builder()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .endpointExposedAnnotation(EndpointExposed.class.getName())
                .addPlugin(new BackbonePlugin()).finish();

        helper.executeParserWithConfig(config);
    }
}
