/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla.internal;

import java.io.File;
import java.net.URL;
import java.util.function.Function;

import com.vaadin.hilla.engine.GeneratorException;
import com.vaadin.hilla.engine.GeneratorProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateEndpoint;

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
        try {
            var engineConfiguration = getEngineConfiguration();
            var processor = new GeneratorProcessor(engineConfiguration,
                    nodeCommand, productionMode);
            processor.process();
        } catch (GeneratorException e) {
            // Make sure the exception is printed in the logs
            LOGGER.error("Failed to run TypeScript endpoint generator", e);
            throw new ExecutionFailedException(
                    "Failed to run TypeScript endpoint generator");
        }
    }

}
