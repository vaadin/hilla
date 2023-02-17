package dev.hilla.endpoint;

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
    public void shouldGenerateExampleEndpointAndEntities() {
        assertTrue(Files.exists(frontendDir.resolve("ExampleEndpoint.ts")));
        assertTrue(Files.exists(frontendDir.resolve("InvalidEndpoint.ts")));
        assertTrue(Files.exists(frontendDir
                .resolve("com/example/application/ExampleEntity.ts")));
        assertTrue(Files.exists(
                frontendDir.resolve("com/external2/ExternalEntity2.ts")));
    }

    /**
     * Endpoint not in com.example.application should not be generated
     */
    @Test
    public void shouldNotGenerateExternalEndpointAndEntities() {
        assertFalse(Files.exists(frontendDir.resolve("ExternalEndpoint.ts")));
        assertFalse(Files
                .exists(frontendDir.resolve("com/external/ExternalEntity.ts")));
    }
}
