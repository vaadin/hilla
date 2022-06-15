package dev.hilla.parser.plugins.model.validation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.model.test.helpers.TestHelper;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.model.ModelPlugin;

public class ValidationTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_GenerateValidations()
            throws IOException, URISyntaxException {
        var config = new ParserConfig.Builder()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new BackbonePlugin()).addPlugin(new ModelPlugin())
                .finish();

        helper.executeParserWithConfig(config);
    }
}
