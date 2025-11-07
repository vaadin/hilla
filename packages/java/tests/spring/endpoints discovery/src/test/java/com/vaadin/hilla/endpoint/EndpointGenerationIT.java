/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.endpoint;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

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
