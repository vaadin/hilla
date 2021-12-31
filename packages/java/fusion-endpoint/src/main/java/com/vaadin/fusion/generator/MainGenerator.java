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
package com.vaadin.fusion.generator;

import java.io.File;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.v3.oas.models.OpenAPI;

import com.vaadin.fusion.generator.typescript.CodeGenerator;

/**
 * Performs the generation of TypeScript files for endpoints, Client API file
 * and endpoint barrel file based on the data from the OpenAPI json. Generation
 * occurs in the directory specified, overwriting the files and creating the
 * target directory, if necessary.
 */
public class MainGenerator {
    public static final String MODEL = "Model";
    public static final String OPTIONAL_SUFFIX = " | undefined";
    public static final String TS = ".ts";
    public static final String MODEL_TS = MODEL + TS;
    private final BarrelGenerator barrelGenerator;
    private final ClientAPIGenerator clientGenerator;
    private final GenerationOutputDirectory outputDirectory;
    private final OpenAPIParser parser;

    /**
     * Initializes the generator.
     *
     * @param openApiJsonFile
     *            the api spec file to analyze
     * @param outputDirectory
     *            the directory to generate the files into
     */
    public MainGenerator(File openApiJsonFile, File outputDirectory) {
        this(openApiJsonFile, outputDirectory, null, null);
    }

    /**
     * Initializes the generator.
     *
     * @param openApiJsonFile
     *            the api spec file to analyze
     * @param outputDirectory
     *            the directory to generate the files into
     * @param properties
     *            the properties with the data required for the Client API file
     *            generation
     */
    public MainGenerator(File openApiJsonFile, File outputDirectory,
            Properties properties) {
        this(openApiJsonFile, outputDirectory, properties, null);
    }

    /**
     * Initializes the generator.
     *
     * @param openApiJsonFile
     *            the api spec file to analyze
     * @param outputDirectory
     *            the directory to generate the files into
     * @param defaultClientPath
     *            the path of the default Client API file which is imported in
     *            the generated files.
     */
    public MainGenerator(File openApiJsonFile, File outputDirectory,
            String defaultClientPath) {
        this(openApiJsonFile, outputDirectory, null, defaultClientPath);
    }

    /**
     * Initializes the generator.
     *
     * @param openApiJsonFile
     *            the api spec file to analyze
     * @param outputDirectory
     *            the directory to generate the files into
     * @param properties
     *            the properties with the data required for the Client API file
     *            generation
     * @param defaultClientPath
     *            the path of the default Client API file which is imported in
     *            the generated files.
     */
    public MainGenerator(File openApiJsonFile, File outputDirectory,
            Properties properties, String defaultClientPath) {
        Objects.requireNonNull(openApiJsonFile);
        Objects.requireNonNull(outputDirectory);

        this.outputDirectory = new GenerationOutputDirectory(outputDirectory);
        parser = openApiJsonFile.exists()
                ? new OpenAPIParser(openApiJsonFile, this.outputDirectory,
                        CodeGenerator.class, defaultClientPath)
                : null;
        clientGenerator = properties != null
                ? new ClientAPIGenerator(this.outputDirectory.toPath(),
                        properties)
                : null;
        barrelGenerator = new BarrelGenerator(this.outputDirectory.toPath());
    }

    /**
     * Starts the generation.
     */
    public void start() {
        if (parser == null) {
            outputDirectory.clean();
            return;
        }

        try {
            OpenAPI openAPI = parser.parseOpenAPI();
            boolean hasGeneratedSuccessfully = generateTypescriptCode(openAPI);

            if (clientGenerator != null && hasGeneratedSuccessfully) {
                clientGenerator.generate();
                barrelGenerator.generate(openAPI);
            }
        } catch (IllegalStateException e) {
            outputDirectory.clean();
            throw e;
        }
    }

    private boolean generateTypescriptCode(OpenAPI openAPI) {
        CodegenConfigurator configurator = parser.getConfigurator();

        Set<File> files = CodeGenerator.generateFiles(
                configurator.toClientOptInput().openAPI(openAPI));

        outputDirectory.clean(files);

        return files.stream()
                .anyMatch(file -> file.getName().endsWith(MainGenerator.TS));
    }
}
