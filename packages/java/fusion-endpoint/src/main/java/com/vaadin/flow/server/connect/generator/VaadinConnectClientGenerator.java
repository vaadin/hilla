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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.frontend.FrontendUtils;

import static com.vaadin.flow.server.connect.generator.VaadinConnectTsGenerator.TS;
/**
 * Generates the Vaadin connect-client file, based on the application
 * properties, if provided.
 */
public class VaadinConnectClientGenerator {
    static final String PREFIX = "vaadin.endpoint.prefix";
    static final String DEFAULT_PREFIX = "/connect";

    static final String URL_MAPPING = "vaadin.urlMapping";
    static final String DEFAULT_URL_MAPPING = "/*";

    private static final String CLIENT_FILE_NAME = "connect-client.default";
    public static final String CONNECT_CLIENT_NAME = CLIENT_FILE_NAME + TS;
    private static final String CUSTOM_CLIENT_FILE_NAME = "connect-client";
    public static final String CUSTOM_CONNECT_CLIENT_NAME = CUSTOM_CLIENT_FILE_NAME + TS;
    public static final String CONNECT_CLIENT_IMPORT_PATH = "./" + CLIENT_FILE_NAME;

    private final String endpointPrefix;

    private static final Logger log = LoggerFactory
            .getLogger(VaadinConnectClientGenerator.class);

    /**
     * Creates the generator, getting the data needed for the generation out of
     * the application properties.
     *
     * @param applicationProperties
     *            the properties with the data required for the generation
     */
    public VaadinConnectClientGenerator(
            Properties applicationProperties) {
        final String prefix =
                (String)applicationProperties.getOrDefault(PREFIX,
                        DEFAULT_PREFIX);
        final String urlMapping = (String) applicationProperties
                .getOrDefault(URL_MAPPING, DEFAULT_URL_MAPPING);

        this.endpointPrefix = relativizeEndpointPrefixWithUrlMapping(prefix, urlMapping);
    }

    final String relativizeEndpointPrefixWithUrlMapping(String endpointPrefix, String urlMapping){
        urlMapping = removeTrailingStar(urlMapping);
        endpointPrefix = removeTrailingStar(endpointPrefix);

        Path urlMappingPath = Paths.get(urlMapping).normalize();
        Path endpointPrefixPath = Paths.get(endpointPrefix).normalize();

        return FrontendUtils.getUnixRelativePath(urlMappingPath, endpointPrefixPath);
    }

    private String removeTrailingStar(String original){
        if(original!=null && original.endsWith("/*")){
            return original.substring(0, original.length() - 1);
        }
        return original;
    }

    /**
     * Generates the client file in the file specified.
     *
     * @param outputFilePath
     *            the file to generate the default client into
     */
    public void generateVaadinConnectClientFile(Path outputFilePath) {
        String generatedDefaultClientTs = getDefaultClientTsTemplate()
                .replace("{{PREFIX}}", endpointPrefix);
        try {
            log.info("writing file {}", outputFilePath);
            FileUtils.writeStringToFile(outputFilePath.toFile(),
                    generatedDefaultClientTs, StandardCharsets.UTF_8);
        } catch (IOException e) {
            String errorMessage = String.format(
                    "Error writing file at %s",
                    outputFilePath.toString());
            log.error(errorMessage, outputFilePath, e);
        }
    }

    private String getDefaultClientTsTemplate() {
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(
                        getClass().getClassLoader().getResourceAsStream(
                                "connect-client.default.ts.template"),
                        StandardCharsets.UTF_8))) {
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to read connect-client.default.ts.template", e);
        }
    }
}
