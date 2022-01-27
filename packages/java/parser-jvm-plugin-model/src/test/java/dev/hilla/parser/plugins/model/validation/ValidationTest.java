package dev.hilla.parser.plugins.model.validation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.model.utils.TestBase;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.model.ModelPlugin;

public class ValidationTest extends TestBase {
    @Test
    public void should_GenerateValidations()
            throws IOException, URISyntaxException {
        var config = new ParserConfig.Builder()
                .classPath(Set.of(targetDir.toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new BackbonePlugin()).addPlugin(new ModelPlugin())
                .finish();

        executeParserWithConfig(config);
    }
}
