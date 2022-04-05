package dev.hilla.parser.plugins.backbone.classmappers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.backbone.utils.TestBase;

public class ClassMappersTest extends TestBase {
    @Test
    public void should_OverrideClassData()
            throws IOException, URISyntaxException {
        var config = new ParserConfig.Builder()
                .classPath(Set.of(targetDir.toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new ClassMappersPlugin())
                .addPlugin(new BackbonePlugin()).finish();

        executeParserWithConfig(config);
    }
}
