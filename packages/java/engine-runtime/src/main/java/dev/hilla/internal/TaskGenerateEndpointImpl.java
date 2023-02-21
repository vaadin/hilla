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

import java.io.File;

import dev.hilla.engine.GeneratorException;
import dev.hilla.engine.GeneratorProcessor;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateEndpoint;

/**
 * Starts the generation of TS files for endpoints.
 */
public class TaskGenerateEndpointImpl extends AbstractTaskEndpointGenerator
        implements TaskGenerateEndpoint {

    /**
     * Create a task for generating OpenAPI spec.
     *
     * @param projectDirectory
     *            the base directory of the project.
     *
     * @param buildDirectoryName
     *            Java build directory name (relative to the {@code
     *              projectDirectory}).
     *
     * @param outputDirectory
     *            the output directory for generated TypeScript code.
     */
    TaskGenerateEndpointImpl(File projectDirectory, String buildDirectoryName,
            File outputDirectory) {
        super(projectDirectory, buildDirectoryName, outputDirectory);
    }

    /**
     * Run TypeScript code generator.
     *
     * @throws ExecutionFailedException
     */
    @Override
    public void execute() throws ExecutionFailedException {
        try {
            var engineConfiguration = getEngineConfiguration();
            var processor = new GeneratorProcessor(engineConfiguration);
            processor.process();
        } catch (GeneratorException e) {
            throw new ExecutionFailedException(
                    "Failed to run TypeScript endpoint generator", e);
        }
    }
}
