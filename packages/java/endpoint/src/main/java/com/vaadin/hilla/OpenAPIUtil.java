package com.vaadin.hilla;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.hilla.engine.EngineAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for interacting with the generated openapi.json.
 */
public class OpenAPIUtil {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(OpenAPIUtil.class);

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
        var openAPIPath = getCurrentOpenAPIPath(buildDirectory,
                isProductionMode);
        if (openAPIPath.isEmpty()) {
            LOGGER.debug(
                    "Trying to read the non-existing OpenApi json file. Empty string is returned.");
            return "";
        }
        return Files.readString(openAPIPath.get());
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
    public static Optional<Path> getCurrentOpenAPIPath(Path buildDirectory,
            boolean isProductionMode) throws IOException {
        var engineConfiguration = new EngineAutoConfiguration.Builder()
                .buildDir(buildDirectory).productionMode(isProductionMode)
                .withDefaultAnnotations().build();
        return Optional.of(engineConfiguration.getOpenAPIFile());
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

        Set<String> types = new HashSet<>();

        // Endpoints
        if (openApi.has("tags")) {
            ArrayNode tags = (ArrayNode) openApi.get("tags");

            if (tags != null) {
                tags.forEach(nameAndClass -> {
                    types.add(nameAndClass.get("x-class-name").asText());
                });
            }
        }

        // Parameters and return types
        if (openApi.has("components")) {
            ObjectNode schemas = (ObjectNode) openApi.get("components")
                    .get("schemas");
            if (schemas != null) {
                schemas.fieldNames().forEachRemaining(type -> {
                    types.add(type);
                });
            }
        }
        return types;

    }
}
