package dev.hilla;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utilities for interacting with the generated openapi.json.
 */
public class OpenAPIUtil {

    /**
     * Parses the given open api and finds all used classes (endpoints,
     * parameter and return types).
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
