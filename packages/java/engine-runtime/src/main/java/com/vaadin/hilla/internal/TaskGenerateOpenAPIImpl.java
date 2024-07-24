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

import javax.annotation.Nonnull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.hilla.ApplicationContextProvider;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.engine.EngineConfiguration;
import com.vaadin.hilla.engine.ParserConfiguration;
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

    private final ClassLoader classLoader;
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
            @Nonnull ClassLoader classLoader, boolean isProductionMode) {
        super(projectDirectory, buildDirectoryName, outputDirectory,
                resourceFinder);
        this.classLoader = Objects.requireNonNull(classLoader,
                "ClassLoader should not be null");
        this.isProductionMode = isProductionMode;
    }

    /**
     * Run Java class parser.
     *
     * @throws ExecutionFailedException
     */
    @Override
    public void execute() throws ExecutionFailedException {
        try {
            var aotOutput = Path.of(System.getProperty("user.dir"),
                    "target/spring-aot/main");
            String groupId = "com.example.application";
            String artifactId = "skeleton-starter-hilla-react";
            var settings = List.of("org.vaadin.example.Application",
                    aotOutput.resolve("sources").toString(),
                    aotOutput.resolve("resources").toString(),
                    aotOutput.resolve("classes").toString(), groupId,
                    artifactId);
            var javaExe = ProcessHandle.current().info().command()
                    .orElse(Path.of(System.getProperty("java.home", "bin/java"))
                            .toString());
            var classPath = String.join(File.pathSeparator,
                    ParserConfiguration.CLASSPATH);

            var processBuilder = new ProcessBuilder();
            processBuilder.inheritIO();
            processBuilder.command(javaExe, "-cp", classPath,
                    "org.springframework.boot.SpringApplicationAotProcessor");
            processBuilder.command().addAll(settings);

            Process process = processBuilder.start();
            process.waitFor();

            var json = aotOutput
                    .resolve(Path.of("resources/META-INF/native-image/",
                            groupId, artifactId, "/reflect-config.json"));
            if (isProductionMode && Files.isRegularFile(json)) {
                try {
                    String jsonContent = Files.readString(json);
                    var objectMapper = new ObjectMapper();
                    var rootNode = objectMapper.readTree(jsonContent);

                    if (rootNode.isArray()) {
                        var candidates = new ArrayList<String>();

                        for (var node : rootNode) {
                            String name = node.get("name").asText();
                            candidates.add(name);
                        }

                        List<Class<?>> endpoints = candidates.stream()
                                .map(name -> {
                                    try {
                                        return Class.forName(name);
                                    } catch (Throwable t) { // must also catch
                                                            // NoClassDefFoundError
                                        return null;
                                    }
                                }).filter(Objects::nonNull)
                                .filter(cls -> cls
                                        .isAnnotationPresent(Endpoint.class)
                                        || cls.isAnnotationPresent(
                                                BrowserCallable.class))
                                .collect(Collectors.toList());
                        var engineConfiguration = new EngineConfiguration();
                        var processor = new ParserProcessor(engineConfiguration,
                                classLoader, isProductionMode);
                        processor.process(endpoints);
                    }
                } catch (IOException e) {
                    throw new ExecutionFailedException(e);
                }
            } else {
                ApplicationContextProvider.runOnContext(applicationContext -> {
                    List<Class<?>> endpoints = Stream
                            .of(BrowserCallable.class, Endpoint.class)
                            .map(applicationContext::getBeansWithAnnotation)
                            .map(Map::values).flatMap(Collection::stream)
                            .map(Object::getClass).distinct()
                            .collect(Collectors.toList());
                    var engineConfiguration = new EngineConfiguration();
                    var processor = new ParserProcessor(engineConfiguration,
                            classLoader, isProductionMode);
                    processor.process(endpoints);
                });
            }
        } catch (Exception e) {
            throw new ExecutionFailedException(e);
        }
    }
}
