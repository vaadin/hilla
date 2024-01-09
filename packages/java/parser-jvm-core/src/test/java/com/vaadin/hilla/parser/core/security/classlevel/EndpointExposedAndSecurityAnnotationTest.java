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

    {
        try {
            classPath = List.of(resourceLoader.findTargetDirPath().toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void throws_when_parentEndpointClass_annotatedWithSecurityAnnotations() {
        var exception = assertThrows(ParserException.class, () -> new Parser()
                .classLoader(getClass().getClassLoader()).classPath(classPath)
                .exposedPackages(Set
                        .of("com.vaadin.hilla.parser.core.security.classlevel"))
                .endpointAnnotation(Endpoint.class.getName())
                .endpointExposedAnnotation(EndpointExposed.class.getName())
                .execute());

        assertTrue(exception.getMessage().startsWith(
                "Class `com.vaadin.hilla.parser.core.security.classlevel.ParentEndpoint` is annotated with `com.vaadin.hilla.parser.core.security.EndpointExposed` and `jakarta.annotation.security.RolesAllowed` annotation."));

    }
}
