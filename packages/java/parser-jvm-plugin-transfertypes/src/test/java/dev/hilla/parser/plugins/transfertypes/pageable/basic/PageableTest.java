package dev.hilla.parser.plugins.transfertypes.pageable.basic;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.transfertypes.TransferTypesPlugin;
import dev.hilla.parser.plugins.transfertypes.utils.TestBase;

public class PageableTest extends TestBase {
    @Test
    public void should_CorrectlyResolveReplacedDependencies()
            throws IOException, URISyntaxException {
        var classpath = getExtendedClassPath(Pageable.class);

        var config = new ParserConfig.Builder()
                .classPath(Set.of(classpath.split(File.pathSeparator)))
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new TransferTypesPlugin())
                .addPlugin(new BackbonePlugin()).finish();

        executeParserWithConfig(config);
    }

    @Test
    public void should_ReplaceSpringClassesWithSubstitutes()
            throws IOException, URISyntaxException {
        var classpath = getExtendedClassPath(Pageable.class);

        var config = new ParserConfig.Builder()
                .classPath(Set.of(classpath.split(File.pathSeparator)))
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new TransferTypesPlugin())
                .addPlugin(new BackbonePlugin()).finish();

        executeParserWithConfig(config);
    }
}
