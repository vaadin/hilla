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
package com.vaadin.hilla.parser.plugins.transfertypes.uuid;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.TransferTypesPlugin;
import com.vaadin.hilla.parser.testutils.FullStackTestHelper;
import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import com.vaadin.hilla.parser.testutils.annotations.EndpointExposed;

/**
 * Full-stack test for UUID type handling - verifies complete Java → TypeScript
 * generation pipeline.
 *
 * This test demonstrates the new testing approach where we: 1. Parse Java code
 * → OpenAPI (via Parser) 2. Generate TypeScript from OpenAPI (via
 * run-generator.mjs) 3. Compare generated TypeScript against snapshots
 *
 * The old approach (UUIDTest) only verified Java → OpenAPI.
 */
public class UUIDFullStackTest {
    private final FullStackTestHelper helper = new FullStackTestHelper(
            getClass());

    @Test
    public void should_ReplaceUUIDClassWithStringInTypeScript()
            throws IOException, URISyntaxException,
            FullStackTestHelper.FullStackExecutionException {
        // Step 1: Parse Java → OpenAPI (same as before)
        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin())
                .addPlugin(new TransferTypesPlugin())
                .execute(List.of(UUIDEndpoint.class));

        // Step 2 & 3: OpenAPI → TypeScript, then verify against snapshots
        var generated = helper.executeFullStack(openAPI);
        helper.assertTypescriptMatches(generated, helper.getSnapshotsDir());
    }
}
