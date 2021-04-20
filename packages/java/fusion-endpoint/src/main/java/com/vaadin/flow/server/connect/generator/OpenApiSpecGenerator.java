/*
 * Copyright 2000-2020 Vaadin Ltd.
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

package com.vaadin.flow.server.connect.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Properties;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generator class that creates the OpenAPI specification file from the
 * sources provided.
 *
 * @see <a href="https://github.com/OAI/OpenAPI-Specification">OpenAPI
 *      specification</a>
 */
public class OpenApiSpecGenerator {
    public static final String APPLICATION_TITLE = "vaadin.connect"
            + ".application.title";
    public static final String APPLICATION_API_VERSION = "vaadin.connect.api.version";
    public static final String SERVER = "vaadin.connect.server";
    public static final String SERVER_DESCRIPTION = "vaadin.connect.server.description";
    public static final String PREFIX = "vaadin.endpoint.prefix";
    public static final String DEFAULT_SERVER = "http://localhost:8080";
    public static final String DEFAULT_SERVER_DESCRIPTION = "Vaadin backend";
    public static final String DEFAULT_APPLICATION_TITLE = "Vaadin application";
    public static final String DEFAULT_APPLICATION_API_VERSION = "0.0.1";
    public static final String DEFAULT_PREFIX = "/connect";

    private static final Logger log = LoggerFactory
            .getLogger(OpenApiSpecGenerator.class);
    private final OpenApiObjectGenerator generator;

    /**
     * Creates the generator, getting the data needed for the generation out of
     * the application properties.
     *
     * @param applicationProperties
     *            the properties with the data required for the generation
     */
    public OpenApiSpecGenerator(Properties applicationProperties) {
        generator = new OpenApiObjectGenerator();
        generator.setOpenApiConfiguration(
                extractOpenApiConfiguration(applicationProperties));
    }

    /**
     * Generates the OpenAPI spec file based on the sources provided.
     *
     * @param sourcesPaths
     *            the source root to be analyzed
     * @param specOutputFile
     *            the target file to write the generation output to
     */
    public void generateOpenApiSpec(Collection<Path> sourcesPaths,
            Path specOutputFile) {
        sourcesPaths.forEach(generator::addSourcePath);
        log.debug("Parsing java files from {}", sourcesPaths);
        OpenAPI openAPI = generator.generateOpenApi();
        try {
            if (openAPI.getPaths().size() > 0) {
                log.debug("writing file {}", specOutputFile);
                FileUtils.writeStringToFile(specOutputFile.toFile(),
                        Json.pretty(openAPI), StandardCharsets.UTF_8);
            } else {
                log.debug("There are no endpoints to generate.");
                FileUtils.deleteQuietly(specOutputFile.toFile());
            }
        } catch (IOException e) {
            String errorMessage = String.format(
                    "Error while writing OpenAPI json file at %s",
                    specOutputFile.toString());
            log.error(errorMessage, specOutputFile, e);
        }
    }

    /**
     * Generates the OpenAPI spec file based on the sources provided.
     *
     * @param sourcesPaths
     *            the source root to be analyzed
     * @param classLoader
     *            the ClassLoader which is able to load the classes in
     *            sourcesPaths
     * @param specOutputFile
     *            the target file to write the generation output to
     */
    public void generateOpenApiSpec(Collection<Path> sourcesPaths,
            ClassLoader classLoader, Path specOutputFile) {
        generator.setTypeResolverClassLoader(classLoader);
        generateOpenApiSpec(sourcesPaths, specOutputFile);
    }

    private OpenApiConfiguration extractOpenApiConfiguration(
            Properties applicationProperties) {
        String prefix = (String) applicationProperties.getOrDefault(PREFIX,
                DEFAULT_PREFIX);
        String server = GeneratorUtils.removeEnd((String) applicationProperties
                .getOrDefault(SERVER, DEFAULT_SERVER), "/");
        String serverDescription = (String) applicationProperties
                .getOrDefault(SERVER_DESCRIPTION, DEFAULT_SERVER_DESCRIPTION);
        String applicationTitle = (String) applicationProperties
                .getOrDefault(APPLICATION_TITLE, DEFAULT_APPLICATION_TITLE);
        String applicationApiVersion = (String) applicationProperties
                .getOrDefault(APPLICATION_API_VERSION,
                        DEFAULT_APPLICATION_API_VERSION);
        return new OpenApiConfiguration(applicationTitle, applicationApiVersion,
                server + prefix, serverDescription);
    }
}
