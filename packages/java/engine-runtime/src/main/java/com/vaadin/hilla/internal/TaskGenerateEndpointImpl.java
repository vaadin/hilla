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
package com.vaadin.hilla.internal;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.vaadin.hilla.engine.GeneratorException;
import com.vaadin.hilla.engine.GeneratorProcessor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.server.frontend.TaskGenerateEndpoint;

/**
 * Starts the generation of TS files for endpoints.
 */
public class TaskGenerateEndpointImpl extends AbstractTaskEndpointGenerator
        implements TaskGenerateEndpoint {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TaskGenerateEndpointImpl.class);

    private final String nodeCommand;
    private boolean productionMode;

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
     * @param productionMode
     *            {@code true} if building for production
     * @param nodeCommand
     *            a command to run NodeJS, either absolute path to the
     *            executable or PATH-related command
     */
    TaskGenerateEndpointImpl(File projectDirectory, String buildDirectoryName,
            File outputDirectory, boolean productionMode, String nodeCommand) {
        super(projectDirectory, buildDirectoryName, outputDirectory);
        this.productionMode = productionMode;
        this.nodeCommand = nodeCommand;
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
            var processor = new GeneratorProcessor(engineConfiguration,
                    nodeCommand);
            processor.process();
        } catch (GeneratorException e) {
            // Make sure the exception is printed in the logs
            LOGGER.error("Failed to run TypeScript endpoint generator", e);
            throw new ExecutionFailedException(
                    "Failed to run TypeScript endpoint generator");
        }
        if (!productionMode) {
            try {
                addDevTools();
            } catch (IOException e) {
                throw new ExecutionFailedException("Failed to add devtools", e);
            }
        }
    }

    private void addDevTools() throws IOException {
        // This is a hack as Hilla does not have any other way to contribute to
        // the generated vaadin.ts
        File vaadinTs = new File(outputDirectory,
                FrontendUtils.BOOTSTRAP_FILE_NAME);
        if (vaadinTs.exists()) {
            String current = FileUtils.readFileToString(vaadinTs,
                    StandardCharsets.UTF_8);
            current += """
                    //@ts-ignore
                    if (import.meta.env.DEV) {
                        import("Frontend/generated/jar-resources/dev-tools-database.js");
                    }
                    """;
            FileUtils.writeStringToFile(vaadinTs, current,
                    StandardCharsets.UTF_8);
        } else {
            LOGGER.error("Unable to add dev tools plugin to vaadin.ts");
        }

    }
}
