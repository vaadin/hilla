package dev.hilla.parser.plugins.backbone.enumtype;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.backbone.utils.TestBase;

public class EnumTypeTest extends TestBase {
    @Test
    public void should_GenerateStringType_When_ReferringToEnumTypes()
            throws IOException, URISyntaxException {
        var config = new ParserConfig.Builder()
                .classPath(Set.of(targetDir.toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new BackbonePlugin()).finish();

        executeParserWithConfig(config);
    }
}
