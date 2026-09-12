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
package com.vaadin.hilla.engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.vaadin.hilla.generator.model.EndpointModel;
import com.vaadin.hilla.generator.model.Generation;
import com.vaadin.hilla.generator.model.MethodModel;
import com.vaadin.hilla.generator.model.TypeModel;

public class TypeScriptProcessorTest {
    private static final Path OUTPUT = Path.of("frontend", "generated");

    private static final Generation GENERATION = new Generation(List
            .of(new EndpointModel("HelloEndpoint", "com.example.HelloEndpoint",
                    List.of(new MethodModel("hello", List.of(),
                            TypeModel.Scalar.of(TypeModel.ScalarKind.STRING,
                                    "java.lang.String"),
                            MethodModel.Kind.CALLED)))),
            List.of(), List.of());

    @TempDir
    private Path baseDir;

    private Path outputDirectory;

    @BeforeEach
    public void resolveOutputDirectory() {
        outputDirectory = baseDir.resolve(OUTPUT);
    }

    @Test
    public void should_WriteTheEndpointsWhereTheApplicationReadsThem()
            throws IOException {
        process(GENERATION);

        assertTrue(Files.isRegularFile(outputDirectory.resolve("endpoints.ts")),
                "The barrel names every endpoint");
        assertTrue(Files.readString(outputDirectory.resolve("HelloEndpoint.ts"))
                .contains("import client from './connect-client.default.js';"),
                "The endpoint calls the server through the generated client");
        assertTrue(
                Files.isRegularFile(
                        outputDirectory.resolve("connect-client.default.ts")),
                "The client is generated along with the endpoints");
    }

    @Test
    public void should_CallTheServerThroughTheClientOfTheApplication()
            throws IOException {
        Files.createDirectories(outputDirectory.getParent());
        Files.writeString(
                baseDir.resolve(Path.of("frontend", "connect-client.ts")),
                "export default {};\n");

        process(GENERATION);

        assertTrue(
                Files.readString(outputDirectory.resolve("HelloEndpoint.ts"))
                        .contains("import client from '../connect-client.js';"),
                "The application has a client of its own");
        assertFalse(
                Files.exists(
                        outputDirectory.resolve("connect-client.default.ts")),
                "Nothing needs a generated client then");
    }

    @Test
    public void should_LeaveNothingBehindWithoutBrowserCallableClasses()
            throws IOException {
        process(GENERATION);

        process(new Generation(List.of(), List.of(), List.of()));

        assertFalse(Files.exists(outputDirectory.resolve("HelloEndpoint.ts")),
                "There is no endpoint to call any more");
        assertFalse(
                Files.exists(outputDirectory
                        .resolve(GeneratorProcessor.GENERATED_FILE_LIST_NAME)),
                "Nothing in the folder was generated");
    }

    private void process(Generation generation) {
        new TypeScriptProcessor(baseDir, OUTPUT).process(generation);
    }
}
