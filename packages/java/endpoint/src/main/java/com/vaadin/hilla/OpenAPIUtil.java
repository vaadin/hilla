package com.vaadin.hilla;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.engine.EngineConfiguration;
import com.vaadin.hilla.engine.ParserProcessor;

/**
 * Utilities for interacting with the generated openapi.json.
 */
public class OpenAPIUtil {

    /**
     * Reads the open api file from the build directory.
     *
     * @param buildDirectory
     *            the build directory, {@code target} if running with Maven
     * @param isProductionMode
     *            whether to generate the openapi for production mode
     * @return the contents of the generated openapi.json
     * @throws IOException
     *             if something went wrong
     */
    public static String getCurrentOpenAPI(Path buildDirectory,
            boolean isProductionMode) throws IOException {
        return Files.readString(
                getCurrentOpenAPIPath(buildDirectory, isProductionMode));
    }

    /**
     * Returns the path to open api file from the build directory.
     *
     * @param buildDirectory
     *            the build directory, {@code target} if running with Maven
     * @param isProductionMode
     *            whether to generate the openapi for production mode
     * @return the Path to the openapi json file
     * @throws IOException
     *             if something went wrong
     */
    public static Path getCurrentOpenAPIPath(Path buildDirectory,
            boolean isProductionMode) throws IOException {
        EngineConfiguration engineConfiguration = EngineConfiguration
                .loadDirectory(buildDirectory);
        return engineConfiguration.getOpenAPIFile(isProductionMode);
    }

    /**
     * Generate a new openapi.json and return it, based on the classes in the
     * build directory.
     *
     * @param buildDirectory
     *            the build directory, {@code target} if running with Maven
     * @param isProductionMode
     *            whether to generate the openapi for production mode
     * @return the contents of the generated openapi.json
     * @throws IOException
     *             if something went wrong
     */
    public static String generateOpenAPI(Path buildDirectory,
            boolean isProductionMode) throws IOException {
        EngineConfiguration engineConfiguration = EngineConfiguration
                .loadDirectory(buildDirectory);
        ParserProcessor parserProcessor = new ParserProcessor(
                engineConfiguration, OpenAPIUtil.class.getClassLoader(),
                isProductionMode);
        return parserProcessor.createOpenAPI();
    }

    /**
     * Parses the given open api and finds all used classes.
     *
     * @param openApiAsText
     *            the open api JSON as text
     * @return a set of classes used
     * @throws IOException
     *             if parsing fails
     */
    public static Set<String> findOpenApiClasses(String openApiAsText)
            throws IOException {
        JsonNode openApi = new ObjectMapper().readTree(openApiAsText);
        if (!openApi.has("components")) {
            return Collections.emptySet();
        }

        Set<String> types = new HashSet<>();

        // Endpoints
        ArrayNode tags = (ArrayNode) openApi.get("tags");

        if (tags != null) {
            tags.forEach(nameAndClass -> {
                types.add(nameAndClass.get("x-class-name").asText());
            });
        }

        // Parameters and return types
        ObjectNode schemas = (ObjectNode) openApi.get("components")
                .get("schemas");
        if (schemas != null) {
            schemas.fieldNames().forEachRemaining(type -> {
                types.add(type);
            });
        }

        return types;

    }
}
