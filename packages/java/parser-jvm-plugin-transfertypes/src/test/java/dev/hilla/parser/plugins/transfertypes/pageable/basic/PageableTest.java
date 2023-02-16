package dev.hilla.parser.plugins.transfertypes.pageable.basic;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import dev.hilla.parser.core.Parser;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.plugins.transfertypes.TransferTypesPlugin;
import dev.hilla.parser.plugins.transfertypes.test.helpers.TestHelper;

public class PageableTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_CorrectlyResolveReplacedDependencies()
            throws IOException, URISyntaxException {
        var classpath = helper.getExtendedClassPath(Pageable.class);

        var openAPI = new Parser().classLoader(getClass().getClassLoader())
                .classPath(classpath.split(File.pathSeparator))
                .endpointAnnotation(Endpoint.class.getName())
                .endpointExposedAnnotation(EndpointExposed.class.getName())
                .addPlugin(new BackbonePlugin())
                .addPlugin(new TransferTypesPlugin()).execute();

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_ReplaceSpringClassesWithSubstitutes()
            throws IOException, URISyntaxException {
        var classpath = helper.getExtendedClassPath(Pageable.class);

        var openAPI = new Parser().classLoader(getClass().getClassLoader())
                .classPath(classpath.split(File.pathSeparator))
                .endpointAnnotation(Endpoint.class.getName())
                .endpointExposedAnnotation(EndpointExposed.class.getName())
                .addPlugin(new BackbonePlugin())
                .addPlugin(new TransferTypesPlugin()).execute();

        helper.executeParserWithConfig(openAPI);
    }
}
