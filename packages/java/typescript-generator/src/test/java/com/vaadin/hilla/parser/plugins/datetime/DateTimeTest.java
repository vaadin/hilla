package com.vaadin.hilla.parser.plugins.backbone.datetime;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.backbone.test.helpers.TestHelper;

public class DateTimeTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_GenerateStringType_When_ReferringToDateTimeTypes()
            throws IOException, URISyntaxException {
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .addPlugin(new BackbonePlugin())
                .execute(List.of(DateTimeEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }
}
