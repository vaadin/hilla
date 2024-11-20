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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.vaadin.hilla.ApplicationContextProvider;
import com.vaadin.hilla.engine.EngineConfiguration;
import com.vaadin.hilla.engine.ParserProcessor;

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

    private final boolean isProductionMode;

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
     * @param resourceFinder
     *            used internally to find resources.
     *
     * @param classLoader
     *            the Java Class Loader for the parser.
     *
     * @param isProductionMode
     *            {@code true} if building for production.
     */
    TaskGenerateOpenAPIImpl(File projectDirectory, String buildDirectoryName,
            File outputDirectory, Function<String, URL> resourceFinder,
            boolean isProductionMode) {
        super(projectDirectory, buildDirectoryName, outputDirectory,
                resourceFinder);
        this.isProductionMode = isProductionMode;
    }

    /**
     * Run Java class parser.
     *
     * @throws ExecutionFailedException
     */
    @Override
    public void execute() throws ExecutionFailedException {
        var engineConfiguration = EngineConfiguration.getDefault();
        if (isProductionMode) {
            var endpoints = engineConfiguration.getOfflineEndpointProvider()
                    .findEndpoints();
            var processor = new ParserProcessor(engineConfiguration, true);
            processor.process(endpoints);
        } else {
            ApplicationContextProvider.runOnContext(applicationContext -> {
                List<Class<?>> endpoints = engineConfiguration.getParser()
                        .getEndpointAnnotations().stream()
                        .map(applicationContext::getBeansWithAnnotation)
                        .map(Map::values).flatMap(Collection::stream)
                        .map(Object::getClass).distinct()
                        .collect(Collectors.toList());
                var processor = new ParserProcessor(engineConfiguration, false);
                processor.process(endpoints);
            });
        }
    }
}
