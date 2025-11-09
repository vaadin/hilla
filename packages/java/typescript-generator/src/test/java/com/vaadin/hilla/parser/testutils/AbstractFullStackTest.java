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
package com.vaadin.hilla.parser.testutils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;

import com.vaadin.hilla.EndpointSubscription;
import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.model.ModelPlugin;
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPlugin;
import com.vaadin.hilla.parser.plugins.subtypes.SubTypesPlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.MultipartFileCheckerPlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.TransferTypesPlugin;
import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import com.vaadin.hilla.parser.testutils.annotations.EndpointExposed;

/**
 * Abstract base class for full-stack tests that verify Java → TypeScript code
 * generation.
 *
 * <p>
 * This class provides a simplified testing approach where all common
 * configuration is pre-configured:
 * <ul>
 * <li>All plugins (Backbone, TransferTypes, Model, Nonnull, SubTypes,
 * MultipartFileChecker)</li>
 * <li>Extended classpath (includes Flux and EndpointSubscription)</li>
 * <li>Both @Endpoint and @EndpointExposed annotations</li>
 * </ul>
 *
 * <p>
 * Usage example:
 *
 * <pre>
 * public class MyEndpointTest extends AbstractFullStackTest {
 *     &#64;Test
 *     public void should_GenerateCorrectTypeScript() throws Exception {
 *         assertTypescriptMatchesSnapshot(MyEndpoint.class);
 *     }
 * }
 * </pre>
 */
public abstract class AbstractFullStackTest {
    protected final FullStackTestHelper helper;

    protected AbstractFullStackTest() {
        this.helper = new FullStackTestHelper(getClass());
    }

    /**
     * Execute the full Java → TypeScript pipeline and verify the generated
     * TypeScript matches the expected snapshots.
     *
     * @param endpointClasses
     *            The endpoint classes to process
     * @throws Exception
     *             if the test fails
     */
    protected void assertTypescriptMatchesSnapshot(Class<?>... endpointClasses)
            throws Exception {
        // Build extended classpath that includes all required dependencies
        var classpath = ResourceLoader.getClasspath(Arrays.stream(
                new Class<?>[] { Flux.class, EndpointSubscription.class })
                .map(ResourceLoader::new).collect(Collectors.toList()));

        // Parse Java → OpenAPI with all plugins
        var openAPI = new Parser()
                .classPath(classpath.split(File.pathSeparator))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin())
                .addPlugin(new TransferTypesPlugin())
                .addPlugin(new ModelPlugin())
                .addPlugin(new NonnullPlugin())
                .addPlugin(new SubTypesPlugin())
                .addPlugin(new MultipartFileCheckerPlugin())
                .execute(Arrays.asList(endpointClasses));

        // OpenAPI → TypeScript, then verify against snapshots
        var generated = helper.executeFullStack(openAPI);
        helper.assertTypescriptMatches(generated, helper.getSnapshotsDir());
    }
}
