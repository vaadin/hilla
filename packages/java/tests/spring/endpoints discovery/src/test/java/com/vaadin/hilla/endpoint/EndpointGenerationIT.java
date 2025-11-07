package com.vaadin.hilla.endpoint;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EndpointGenerationIT {
    Path frontendDir = Path.of("src/main/frontend/generated");

    /**
     * Endpoint in com.example.application should be generated
     */
    @Test
    public void shouldGenerateApplicationEndpoint() {
        assertTrue(Files.exists(frontendDir.resolve("ExampleEndpoint.ts")));
        assertTrue(Files.exists(frontendDir
                .resolve("com/example/application/ExampleEntity.ts")));
        assertTrue(Files.exists(
                frontendDir.resolve("com/external2/ExternalEntity2.ts")));
    }

    /**
     * As we are now using the lookup-based endpoint finder, all the endpoints
     * are generated, even those that will not be available as Spring beans.
     */
    @Test
    public void shouldGenerateExternalEndpoint() {
        assertTrue(Files.exists(frontendDir.resolve("ExternalEndpoint.ts")));
        assertTrue(Files
                .exists(frontendDir.resolve("com/external/ExternalEntity.ts")));
    }

    /**
     * Endpoint in system dependencies should be generated, provided that the
     * package is whitelisted in configuration
     */
    @Test
    public void shouldGenerateEndpointFromSystemDependencies() {
        assertTrue(Files.exists(frontendDir.resolve("SomeEndpoint.ts")));
    }
}
