package com.vaadin.hilla.parser.plugins.transfertypes.push;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.EndpointSubscription;
import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.TransferTypesPlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.test.helpers.TestHelper;
import reactor.core.publisher.Flux;

public class PushTypeTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_ReplacePushTypes()
            throws IOException, URISyntaxException {
        var classpath = helper.getExtendedClassPath(Flux.class,
                EndpointSubscription.class);

        var openAPI = new Parser()
                .classPath(classpath.split(File.pathSeparator))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin())
                .addPlugin(new TransferTypesPlugin())
                .execute(List.of(OtherEndpoint.class, PushTypeEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }
}
