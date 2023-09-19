package dev.hilla.parser.plugins.model.annotations;

import dev.hilla.parser.core.Parser;
import dev.hilla.parser.model.test.helpers.TestHelper;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.model.Endpoint;
import dev.hilla.parser.plugins.model.EndpointExposed;
import dev.hilla.parser.plugins.model.ModelPlugin;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

public class AnnotationsTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_GenerateAnnotations()
            throws IOException, URISyntaxException {
        var openAPI = new Parser().classLoader(getClass().getClassLoader())
                .exposedPackages(
                        Set.of("dev.hilla.parser.plugins.model.annotations"))
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .endpointExposedAnnotation(EndpointExposed.class.getName())
                .addPlugin(new BackbonePlugin()).addPlugin(new ModelPlugin())
                .execute();

        helper.executeParserWithConfig(openAPI);
    }
}
