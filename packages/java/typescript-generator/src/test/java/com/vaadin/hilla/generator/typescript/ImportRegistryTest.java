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
package com.vaadin.hilla.generator.typescript;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ImportRegistryTest {
    private final ImportRegistry imports = new ImportRegistry();

    @Test
    public void should_ReuseTheNameOfSomethingAlreadyImported() {
        var first = imports.importDefault("./Person.js", "Person", true);
        var second = imports.importDefault("./Person.js", "Person", true);

        assertEquals(first, second);
        assertEquals(List.of("import type Person from './Person.js';"),
                imports.write());
    }

    @Test
    public void should_GiveDistinctNamesToClashingImports() {
        var first = imports.importDefault("./one/Person.js", "Person", true);
        var second = imports.importDefault("./two/Person.js", "Person", true);

        assertEquals("Person", first);
        assertEquals("Person_1", second);
    }

    @Test
    public void should_KeepAValueImportWhenTheSameThingIsAlsoNeededAsAType() {
        imports.importDefault("./client.js", "client", true);
        imports.importDefault("./client.js", "client", false);

        assertEquals(List.of("import client from './client.js';"),
                imports.write());
    }

    @Test
    public void should_WriteBareModulesBeforeRelativeOnes() {
        imports.importDefault("./client.js", "client", false);
        imports.importNamed("@vaadin/hilla-frontend", "EndpointRequestInit",
                true);
        imports.importAll("./PersonEndpoint.js", "PersonEndpoint");

        assertEquals(List.of(
                "import type { EndpointRequestInit } from '@vaadin/hilla-frontend';",
                "import * as PersonEndpoint from './PersonEndpoint.js';",
                "import client from './client.js';"), imports.write());
    }
}
