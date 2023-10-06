package dev.hilla.parser.core.security.methodlevel;

import dev.hilla.parser.core.Parser;
import dev.hilla.parser.core.ParserException;
import dev.hilla.parser.core.basic.Endpoint;
import dev.hilla.parser.core.security.EndpointExposed;
import dev.hilla.parser.testutils.ResourceLoader;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EndpointExposedMethodAndSecurityAnnotationTest {

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
    public void throws_when_parentEndpointMethod_annotatedWithSecurityAnnotations() {
        var exception = assertThrows(ParserException.class, () -> new Parser()
                .classLoader(getClass().getClassLoader()).classPath(classPath)
                .exposedPackages(
                        Set.of("dev.hilla.parser.core.security.methodlevel"))
                .endpointAnnotation(Endpoint.class.getName())
                .endpointExposedAnnotation(EndpointExposed.class.getName())
                .execute());

        assertTrue(exception.getMessage().startsWith(
                "Class `dev.hilla.parser.core.security.methodlevel.ParentEndpoint` is annotated with `dev.hilla.parser.core.security.EndpointExposed` and `jakarta.annotation.security.RolesAllowed` annotation."));

    }
}
