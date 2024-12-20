package com.vaadin.hilla.parser.core.security.classlevel;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.core.ParserException;
import com.vaadin.hilla.parser.core.basic.Endpoint;
import com.vaadin.hilla.parser.core.security.EndpointExposed;
import com.vaadin.hilla.parser.testutils.ResourceLoader;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EndpointExposedAndSecurityAnnotationTest {

    private final List<String> classPath;
    private final ResourceLoader resourceLoader = new ResourceLoader(
            getClass());
    private final List<Class<?>> endpoints = List.of(SomeEndpoint.class);

    {
        try {
            classPath = List.of(resourceLoader.findTargetDirPath().toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void throws_when_parentEndpointClass_annotatedWithSecurityAnnotations() {
        var exception = assertThrows(ParserException.class,
                () -> new Parser().classPath(classPath)
                        .endpointAnnotations(List.of(Endpoint.class))
                        .endpointExposedAnnotations(
                                List.of(EndpointExposed.class))
                        .execute(endpoints));

        assertTrue(exception.getMessage().startsWith(
                "Class `com.vaadin.hilla.parser.core.security.classlevel.ParentEndpoint` is annotated with `com.vaadin.hilla.parser.core.security.EndpointExposed` and `jakarta.annotation.security.RolesAllowed` annotation."));

    }
}
