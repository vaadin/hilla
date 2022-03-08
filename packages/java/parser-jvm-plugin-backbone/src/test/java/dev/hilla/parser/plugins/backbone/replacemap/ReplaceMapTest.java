package dev.hilla.parser.plugins.backbone.replacemap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.backbone.utils.TestBase;

public class ReplaceMapTest extends TestBase {
    @Test
    public void should_useReplaceMapToOverrideClassData() throws IOException, URISyntaxException {
        var config = new ParserConfig.Builder()
                .classPath(Set.of(targetDir.toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new ReplaceMapPlugin())
                .addPlugin(new BackbonePlugin()).finish();

        executeParserWithConfig(config);
    }
}
