package dev.hilla.parser.plugins.pageable.basic;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.pageable.PageablePlugin;
import dev.hilla.parser.plugins.pageable.utils.TestBase;

public class PageableTest extends TestBase {
    @Test
    public void should_ReplaceSpringClassesWithSubstitutes()
            throws IOException, URISyntaxException {
        var classpath = System.getProperty("java.class.path");

        var config = new ParserConfig.Builder()
                .classPath(Set.of(classpath.split(";")))
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new BackbonePlugin())
                .finish();

        executeParserWithConfig(config);
    }
}
