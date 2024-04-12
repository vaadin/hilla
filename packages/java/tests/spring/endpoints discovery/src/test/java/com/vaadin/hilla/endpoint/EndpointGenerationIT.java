package com.vaadin.hilla.endpoint;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EndpointGenerationIT {
    Path frontendDir = Paths.get("frontend", "generated");

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
     * Endpoint in configured packages should be generated
     */
    @Test
    public void shouldGenerateConfiguredEndpoint() {
        assertTrue(Files.exists(frontendDir.resolve("InvalidEndpoint.ts")));
        assertTrue(Files.exists(frontendDir
                .resolve("com/example/library/unpublished/SomeEntity.ts")));
    }

    /**
     * Endpoint not in configured packages should not be generated
     */
    @Test
    public void shouldNotGenerateExternalEndpoint() {
        assertFalse(Files.exists(frontendDir.resolve("ExternalEndpoint.ts")));
        assertFalse(Files
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
