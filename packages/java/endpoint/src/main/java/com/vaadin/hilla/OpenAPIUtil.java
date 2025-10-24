package com.vaadin.hilla;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for interacting with the generated openapi.json.
 *
 * @deprecated OpenAPI generation has been removed from Hilla. TypeScript is now
 *             generated directly from Java classes without using OpenAPI as an
 *             intermediate format. These methods are kept for backward
 *             compatibility but will return empty results.
 */
@Deprecated(since = "25.0", forRemoval = true)
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
     * @deprecated OpenAPI is no longer generated. This method always returns an
     *             empty string.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    public static String getCurrentOpenAPI(Path buildDirectory,
            boolean isProductionMode) throws IOException {
        LOGGER.warn(
                "OpenAPI generation has been removed. getCurrentOpenAPI() always returns empty string.");
        return "";
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
     * @deprecated OpenAPI is no longer generated. This method always returns
     *             Optional.empty().
     */
    @Deprecated(since = "25.0", forRemoval = true)
    public static Optional<Path> getCurrentOpenAPIPath(Path buildDirectory,
            boolean isProductionMode) throws IOException {
        LOGGER.warn(
                "OpenAPI generation has been removed. getCurrentOpenAPIPath() always returns empty Optional.");
        return Optional.empty();
    }

    /**
     * Parses the given open api and finds all used classes.
     *
     * @param openApiAsText
     *            the open api JSON as text
     * @return a set of classes used
     * @throws IOException
     *             if parsing fails
     * @deprecated OpenAPI is no longer generated. This method can still parse
     *             legacy OpenAPI files if needed, but will not work with newly
     *             generated endpoints.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    public static Set<String> findOpenApiClasses(String openApiAsText)
            throws IOException {
        if (openApiAsText == null || openApiAsText.isEmpty()) {
            LOGGER.warn(
                    "OpenAPI generation has been removed. findOpenApiClasses() returns empty set.");
            return new HashSet<>();
        }

        // Keep parsing logic for backward compatibility with existing OpenAPI
        // files
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
            var components = openApi.get("components");
            if (components != null && components.has("schemas")) {
                var schemasNode = components.get("schemas");
                if (schemasNode instanceof ObjectNode schemas) {
                    for (String type : schemas.propertyNames()) {
                        types.add(type);
                    }
                }
            }
        }
        return types;

    }
}
