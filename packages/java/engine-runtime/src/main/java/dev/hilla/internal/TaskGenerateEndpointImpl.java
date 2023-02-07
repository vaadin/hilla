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
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateEndpoint;

/**
 * Starts the generation of TS files for endpoints.
 */
@NpmPackage(value = "@hilla/generator-typescript-core", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-utils", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-cli", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-plugin-client", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-plugin-backbone", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-plugin-barrel", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-plugin-model", version = "2.0.0-beta1")
@NpmPackage(value = "@hilla/generator-typescript-plugin-push", version = "2.0.0-beta1")
public class TaskGenerateEndpointImpl extends AbstractTaskEndpointGenerator
    implements TaskGenerateEndpoint {
    private final File outputDirectory;
    private final File openAPI;

    TaskGenerateEndpointImpl(File applicationProperties,
        File projectDirectory,
        String buildDirectoryName,
        File openAPI, File outputDirectory) {

        super(applicationProperties, projectDirectory, buildDirectoryName);
        Objects.requireNonNull(openAPI,
            "OpenAPI file cannot be null");
        Objects.requireNonNull(outputDirectory,
            "Output directory cannot be null");
        this.openAPI = openAPI;
        this.outputDirectory = outputDirectory;
    }

    private String readOpenApi() throws ExecutionFailedException {
        try {
            return Files.readString(openAPI.toPath());
        } catch (IOException e) {
            throw new ExecutionFailedException("Failed to read OpenAPI spec file " + openAPI.getPath(), e);
        }
    }

    @Override
    public void execute() throws ExecutionFailedException {
        try {
            var engineConfiguration = getEngineConfiguration();
            var processor = new GeneratorProcessor(engineConfiguration.getBaseDir())
                .outputDir(outputDirectory);
            getEngineConfiguration().getGenerator().getPlugins().ifPresent(processor::plugins);
            processor.input(readOpenApi()).process();
        } catch (IOException | InterruptedException | GeneratorUnavailableException e) {
            throw new ExecutionFailedException(
                "Failed to run TypeScript endpoint generator", e);
        }
    }
}
