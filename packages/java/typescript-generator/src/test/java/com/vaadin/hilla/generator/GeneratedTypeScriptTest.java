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
package com.vaadin.hilla.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.generator.fixtures.EmptyEndpoint;
import com.vaadin.hilla.generator.model.EndpointModel;
import com.vaadin.hilla.generator.model.Generation;
import com.vaadin.hilla.generator.model.MethodModel;
import com.vaadin.hilla.generator.typescript.BarrelWriter;
import com.vaadin.hilla.generator.typescript.GeneratedFile;
import com.vaadin.hilla.generator.typescript.TypeScriptWriter;
import com.vaadin.hilla.parser.plugins.sample.SampleEndpoint;
import com.vaadin.hilla.parser.testutils.FullStackGenerator;

/**
 * Verifies what the snapshots of the generated TypeScript cannot show: which
 * files a generation is written to, and what is written from a model no walk of
 * a test case builds.
 *
 * <p>
 * The TypeScript of a browser callable class is verified by the snapshot test
 * cases under {@code com.vaadin.hilla.parser.plugins}.
 */
public class GeneratedTypeScriptTest {
    private static final Generation generation = generationOf(
            SampleEndpoint.class);

    @Test
    public void should_WriteTheClientOnlyWhenItIsTheGeneratedOne() {
        var endpointsOnly = new Generation(generation.endpoints(), List.of(),
                List.of());

        assertTrue(
                pathsOf(new TypeScriptWriter().write(endpointsOnly))
                        .contains("connect-client.default.ts"),
                "The endpoints have no client to call the server with"
                        + " otherwise");

        var files = new TypeScriptWriter("../custom-client.js")
                .write(endpointsOnly);

        assertFalse(pathsOf(files).contains("connect-client.default.ts"),
                "The application has a client of its own");
        assertTrue(
                content(files, "SampleEndpoint.ts")
                        .contains("import client from '../custom-client.js';"),
                "The endpoints call the server with the client of the"
                        + " application");
    }

    @Test
    public void should_WriteAnEmptyBarrelWithoutEndpoints() {
        assertEquals("export {};\n",
                new BarrelWriter().write(List.of()).content());
    }

    @Test
    public void should_NameAnEndpointOnceHoweverManyClassesGoByIt() {
        // Two classes of the same name, in packages of their own, are one
        // endpoint as far as the browser is concerned: the server answers
        // calls of that name with one of them, and the barrel would otherwise
        // export the name twice, which TypeScript reads as two declarations
        var generation = generationOf(EmptyEndpoint.class,
                com.vaadin.hilla.generator.fixtures.other.EmptyEndpoint.class);

        assertEquals(List.of("EmptyEndpoint"), generation.endpoints().stream()
                .map(EndpointModel::name).toList());

        var endpoint = generation.endpoints().get(0);

        assertEquals(
                com.vaadin.hilla.generator.fixtures.other.EmptyEndpoint.class
                        .getName(),
                endpoint.javaClass(),
                "The last class of the name is the one the server answers with");
        assertEquals(List.of("describe"),
                endpoint.methods().stream().map(MethodModel::name).toList(),
                "Which is the class the methods are written from");
    }

    @Test
    public void should_ExportAnEndpointUnderItsOwnName() {
        var written = new BarrelWriter().write(List.of(
                new EndpointModel("Record", "com.example.Record", List.of())));

        assertEquals("""
                import * as Record_1 from './Record.js';

                export { Record_1 as Record };
                """, written.content());
    }

    @Test
    public void should_ReExportEveryEndpointInTheSameOrderWhicheverItIsGiven() {
        // Given the other way around than they are written, so that the order
        // is the one the barrel decides rather than the one it was handed
        var written = new BarrelWriter()
                .write(List.of(generation.endpoints().get(0),
                        generationOf(EmptyEndpoint.class).endpoints().get(0)));

        assertEquals("""
                import * as EmptyEndpoint from './EmptyEndpoint.js';
                import * as SampleEndpoint from './SampleEndpoint.js';

                export { EmptyEndpoint, SampleEndpoint };
                """, written.content());
    }

    @Test
    public void should_DescribeOnlyTheLastRunOfTheParser() {
        var generator = new FullStackGenerator(GeneratedTypeScriptTest.class,
                SampleEndpoint.class);
        generator.parseGeneration();
        var generation = generator.parseGeneration();

        assertEquals(1, generation.endpoints().size());
        assertEquals(generationOf(SampleEndpoint.class), generation,
                "What the first run found is not found again by the second");
    }

    private static Generation generationOf(Class<?>... endpoints) {
        return new FullStackGenerator(GeneratedTypeScriptTest.class, endpoints)
                .parseGeneration();
    }

    private static List<String> pathsOf(List<GeneratedFile> files) {
        return files.stream().map(GeneratedFile::path).toList();
    }

    private static String content(List<GeneratedFile> files, String path) {
        return files.stream().filter(file -> file.path().equals(path))
                .findFirst()
                .orElseThrow(
                        () -> new AssertionError(path + " was not written"))
                .content();
    }
}
