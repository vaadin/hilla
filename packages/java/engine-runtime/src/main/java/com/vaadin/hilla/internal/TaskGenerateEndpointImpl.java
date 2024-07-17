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
import java.net.URL;
import java.util.function.Function;

import com.vaadin.hilla.ApplicationContextProvider;
import com.vaadin.hilla.engine.GeneratorException;
import com.vaadin.hilla.engine.GeneratorProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateEndpoint;
import com.vaadin.hilla.engine.EngineConfiguration;

/**
 * Starts the generation of TS files for endpoints.
 */
public class TaskGenerateEndpointImpl extends AbstractTaskEndpointGenerator
        implements TaskGenerateEndpoint {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TaskGenerateEndpointImpl.class);

    private final String nodeCommand;
    private final boolean productionMode;

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
     * @param resourceFinder
     *            used internally to find resources
     * @param productionMode
     *            {@code true} if building for production
     * @param nodeCommand
     *            a command to run NodeJS, either absolute path to the
     *            executable or PATH-related command
     */
    TaskGenerateEndpointImpl(File projectDirectory, String buildDirectoryName,
            File outputDirectory, Function<String, URL> resourceFinder,
            boolean productionMode, String nodeCommand) {
        super(projectDirectory, buildDirectoryName, outputDirectory,
                resourceFinder);
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
        if (productionMode) {
            var engineConfiguration = new EngineConfiguration();
            var processor = new GeneratorProcessor(engineConfiguration,
                    nodeCommand, productionMode);
            processor.process();
        } else {
            ApplicationContextProvider.runOnContext(applicationContext -> {
                var engineConfiguration = new EngineConfiguration();
                var processor = new GeneratorProcessor(engineConfiguration,
                        nodeCommand, productionMode);
                processor.process();
            });
        }
    }

}
