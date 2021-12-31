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
import java.util.List;

import io.swagger.codegen.v3.auth.AuthParser;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import static com.vaadin.fusion.generator.MainGenerator.TS;

/**
 * Parses the OpenAPI file to an object to make it consumable for other tools.
 */
class OpenAPIParser {
    private static final String CLIENT_PATH_TEMPLATE_PROPERTY = "vaadinConnectDefaultClientPath";
    private final CodegenConfigurator configurator;

    /**
     * Prepares the OpenAPI file for reading by other tools.
     *
     * @param openApiJsonFile
     *            the api spec file to analyze
     * @param outputDirectory
     *            the directory to generateOpenApiSpec the files into
     * @see <a href="https://github.com/OAI/OpenAPI-Specification">OpenAPI
     *      specification</a>
     */
    OpenAPIParser(File openApiJsonFile,
            GenerationOutputDirectory outputDirectory, Class<?> language) {
        this(openApiJsonFile, outputDirectory, language, null);
    }

    /**
     * Prepares the OpenAPI file for reading by other tools.
     *
     * @param openApiJsonFile
     *            the api spec file to analyze
     * @param outputDirectory
     *            the directory to generateOpenApiSpec the files into
     * @param defaultClientPath
     *            the default client path which is imported in the generated
     *            files. If it is {@code null}, the default generate client path
     *            is used.
     * @see <a href="https://github.com/OAI/OpenAPI-Specification">OpenAPI
     *      specification</a>
     */
    OpenAPIParser(File openApiJsonFile,
            GenerationOutputDirectory outputDirectory, Class<?> language,
            String defaultClientPath) {
        configurator = new CodegenConfigurator();
        configurator.setLang(language.getName());
        configurator.setInputSpecURL(openApiJsonFile.toString());
        configurator.setOutputDir(outputDirectory.toString());
        configurator.addAdditionalProperty(CLIENT_PATH_TEMPLATE_PROPERTY,
                getDefaultClientPath(defaultClientPath));
    }

    private static String getDefaultClientPath(String path) {
        path = path != null ? path
                : ClientAPIGenerator.CONNECT_CLIENT_IMPORT_PATH;
        return path.endsWith(TS)
                ? path.substring(0, path.length() - TS.length())
                : path;
    }

    CodegenConfigurator getConfigurator() {
        return configurator;
    }

    OpenAPI parseOpenAPI() {
        SwaggerParseResult result;

        try {
            List<AuthorizationValue> authorizationValues = AuthParser
                    .parse(configurator.getAuth());
            String inputSpec = configurator.loadSpecContent(
                    configurator.getInputSpecURL(), authorizationValues);
            ParseOptions options = new ParseOptions();
            options.setResolve(true);

            result = new io.swagger.parser.OpenAPIParser()
                    .readContents(inputSpec, authorizationValues, options);
        } catch (Exception e) {
            throw new IllegalStateException(String.format(
                    "Can't read file '%s'", configurator.getInputSpecURL()), e);
        }

        if (result == null || !result.getMessages().isEmpty()) {
            String error = result == null ? ""
                    : String.join("", result.getMessages());

            throw new IllegalStateException(
                    "Unexpected error while generating Vaadin TypeScript endpoint wrappers."
                            + " The input file "
                            + configurator.getInputSpecURL()
                            + " might be corrupted, please try running the generating tasks again. "
                            + error);
        }

        OpenAPI openAPI = result.getOpenAPI();

        if (openAPI.getComponents() == null) {
            openAPI.setComponents(new Components());
        }

        return openAPI;
    }
}
