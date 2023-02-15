/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package dev.hilla.internal;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import dev.hilla.engine.ParserException;
import dev.hilla.engine.ParserProcessor;
import dev.hilla.parser.utils.OpenAPIPrinter;
import io.swagger.v3.oas.models.OpenAPI;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateOpenAPI;

/**
 * Generate OpenAPI json file for Vaadin Endpoints.
 */
public class TaskGenerateOpenAPIImpl extends AbstractTaskEndpointGenerator
        implements TaskGenerateOpenAPI {

    private File output;
    private ClassLoader classLoader;

    /**
     * Create a task for generating OpenAPI spec.
     *
     * @param output
     *            the output path of the generated json file.
     */
    TaskGenerateOpenAPIImpl(File projectDirectory, String buildDirectoryName,
            @Nonnull ClassLoader classLoader, @Nonnull File output) {
        super(projectDirectory, buildDirectoryName);
        this.output = Objects.requireNonNull(output,
                "OpenAPI output file should not be null");
        this.classLoader = Objects.requireNonNull(classLoader,
                "ClassLoader should not be null");
    }

    @Override
    public void execute() throws ExecutionFailedException {
        try {
            var engineConfiguration = getEngineConfiguration();
            var processor = new ParserProcessor(
                engineConfiguration.getBaseDir(), classLoader,
                engineConfiguration.getClassPath())
                .apply(engineConfiguration.getParser());

            var openAPI = processor.process();
            writeOpenAPI(openAPI);
        } catch (ParserException e) {
            throw new ExecutionFailedException("Java code parsing failed", e);
        }
    }

    private void writeOpenAPI(OpenAPI openAPI) throws ExecutionFailedException {
        try {
            var openAPIFile = output.toPath();
            Files.createDirectories(openAPIFile.getParent());

            Files.write(openAPIFile, new OpenAPIPrinter().pretty()
                    .writeAsString(openAPI).getBytes());
        } catch (IOException e) {
            throw new ExecutionFailedException(
                    "Failed to write OpenAPI spec file", e);
        }
    }
}
