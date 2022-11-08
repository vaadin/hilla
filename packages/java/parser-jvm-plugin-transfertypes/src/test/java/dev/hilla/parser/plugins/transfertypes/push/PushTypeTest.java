package dev.hilla.parser.plugins.transfertypes.push;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import dev.hilla.EndpointSubscription;
import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.transfertypes.TransferTypesPlugin;
import dev.hilla.parser.plugins.transfertypes.test.helpers.TestHelper;
import reactor.core.publisher.Flux;

public class PushTypeTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_ReplacePushTypes()
            throws IOException, URISyntaxException {
        var classpath = helper.getExtendedClassPath(Flux.class,
                EndpointSubscription.class);

        var config = new ParserConfig.Builder()
                .classPath(classpath.split(File.pathSeparator))
                .endpointAnnotation(Endpoint.class.getName())
                .endpointExposedAnnotation(EndpointExposed.class.getName())
                .addPlugin(new TransferTypesPlugin())
                .addPlugin(new BackbonePlugin()).finish();

        helper.executeParserWithConfig(config);
    }
}
