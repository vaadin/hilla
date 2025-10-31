package com.vaadin.hilla.parser.plugins.transfertypes.pageable.bare;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.TransferTypesPlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.test.helpers.TestHelper;

public class BarePageableTest {
    private final TestHelper helper = new TestHelper(getClass());
    private final List<Class<?>> endpoints = List
            .of(BarePageableEndpoint.class);

    @Test
    public void should_ConsiderInternalDependenciesForReplacedEntities()
            throws IOException, URISyntaxException {
        var classpath = helper.getExtendedClassPath(Pageable.class);

        var openAPI = new Parser()
                .classPath(classpath.split(File.pathSeparator))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin())
                .addPlugin(new TransferTypesPlugin()).execute(endpoints);

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void should_CorrectlyResolveReplacedDependencies()
            throws IOException, URISyntaxException {
        var classpath = helper.getExtendedClassPath(Pageable.class);

        var openAPI = new Parser()
                .classPath(classpath.split(File.pathSeparator))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin())
                .addPlugin(new TransferTypesPlugin()).execute(endpoints);

        helper.executeParserWithConfig(openAPI);
    }
}
