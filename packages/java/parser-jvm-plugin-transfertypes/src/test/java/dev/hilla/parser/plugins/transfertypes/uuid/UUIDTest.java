package dev.hilla.parser.plugins.transfertypes.uuid;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.transfertypes.TransferTypesPlugin;
import dev.hilla.parser.plugins.transfertypes.utils.TestBase;

public class UUIDTest extends TestBase {
    @Test
    public void should_ReplaceUUIDClassWithString()
            throws IOException, URISyntaxException {
        var config = new ParserConfig.Builder()
                .classPath(Set.of(targetDir.toString()))
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new TransferTypesPlugin())
                .addPlugin(new BackbonePlugin()).finish();

        executeParserWithConfig(config);
    }
}
