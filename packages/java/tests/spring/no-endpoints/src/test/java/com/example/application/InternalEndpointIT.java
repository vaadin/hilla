package com.example.application;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InternalEndpointIT {
    Path frontendDir = Paths.get("frontend", "generated");

    @Test
    public void shouldNotRunEndpointGeneratorForOnlyInternalEndpointsPresent() {
        assertFalse(Files.exists(frontendDir.resolve("InternalBrowserCallableExample.ts")));
        assertFalse(Files.exists(frontendDir.resolve("InternalEndpointExample.ts")));
        assertFalse(Files.exists(frontendDir
                .resolve("com/example/application/InternalBrowserCallableExample.ts")));
        assertFalse(Files.exists(frontendDir
            .resolve("com/example/application/InternalEndpointExample.ts")));
    }


}
