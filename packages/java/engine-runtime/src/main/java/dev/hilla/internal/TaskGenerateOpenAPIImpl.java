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
import java.util.Objects;

import dev.hilla.engine.ParserException;
import dev.hilla.engine.ParserProcessor;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateOpenAPI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate OpenAPI json file for Vaadin Endpoints.
 */
public class TaskGenerateOpenAPIImpl extends AbstractTaskEndpointGenerator
        implements TaskGenerateOpenAPI {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TaskGenerateOpenAPIImpl.class);

    private final ClassLoader classLoader;

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
     *
     * @param classLoader
     *            the Java Class Loader for the parser.
     */
    TaskGenerateOpenAPIImpl(File projectDirectory, String buildDirectoryName,
            File outputDirectory, @Nonnull ClassLoader classLoader) {
        super(projectDirectory, buildDirectoryName, outputDirectory);
        this.classLoader = Objects.requireNonNull(classLoader,
                "ClassLoader should not be null");
    }

    /**
     * Run Java class parser.
     *
     * @throws ExecutionFailedException
     */
    @Override
    public void execute() throws ExecutionFailedException {
        try {
            var engineConfiguration = getEngineConfiguration();
            var processor = new ParserProcessor(engineConfiguration,
                    classLoader);
            processor.process();
        } catch (ParserException e) {
            // Make sure the exception is printed in the logs
            LOGGER.error("Java code parsing failed", e);
            throw new ExecutionFailedException("Java code parsing failed");
        }
    }
}
