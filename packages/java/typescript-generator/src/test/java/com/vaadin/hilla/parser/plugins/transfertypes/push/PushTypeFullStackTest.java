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
package com.vaadin.hilla.parser.plugins.transfertypes.push;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import com.vaadin.hilla.EndpointSubscription;
import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.TransferTypesPlugin;
import com.vaadin.hilla.parser.testutils.FullStackTestHelper;
import com.vaadin.hilla.parser.testutils.ResourceLoader;
import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import com.vaadin.hilla.parser.testutils.annotations.EndpointExposed;

/**
 * Full-stack test for Push/Flux type handling - verifies complete Java →
 * TypeScript generation pipeline.
 */
public class PushTypeFullStackTest {
    private final FullStackTestHelper helper = new FullStackTestHelper(
            getClass());

    @Test
    public void should_ReplacePushTypes()
            throws IOException, URISyntaxException,
            FullStackTestHelper.FullStackExecutionException {
        // Build extended classpath for Flux and EndpointSubscription
        var classpath = ResourceLoader.getClasspath(Arrays.stream(
                new Class<?>[] { Flux.class, EndpointSubscription.class })
                .map(ResourceLoader::new).collect(Collectors.toList()));

        // Step 1: Parse Java → OpenAPI
        var openAPI = new Parser()
                .classPath(classpath.split(File.pathSeparator))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin())
                .addPlugin(new TransferTypesPlugin())
                .execute(List.of(OtherEndpoint.class, PushTypeEndpoint.class));

        // Step 2 & 3: OpenAPI → TypeScript, verify against snapshots
        var generated = helper.executeFullStack(openAPI);
        helper.assertTypescriptMatches(generated, helper.getSnapshotsDir());
    }
}
