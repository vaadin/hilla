package dev.hilla;

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
import dev.hilla.engine.EngineConfiguration;
import dev.hilla.engine.ParserProcessor;

/**
 * Utilities for interacting with the generated openapi.json.
 */
public class OpenAPIUtil {

    /**
     * Reads the open api file from the build directory.
     *
     * @param buildDirectory
     *            the build directory, {@code target} if running with Maven
     * @return the contents of the generated openapi.json
     * @throws IOException
     *             if something went wrong
     */
    public static String getCurrentOpenAPI(Path buildDirectory)
            throws IOException {
        EngineConfiguration engineConfiguration = EngineConfiguration
                .loadDirectory(buildDirectory);
        return Files.readString(engineConfiguration.getOpenAPIFile());
    }

    /**
     * Generate a new openapi.json and return it, based on the classes in the
     * build directory.
     *
     * @param buildDirectory
     *            the build directory, {@code target} if running with Maven
     * @return the contents of the generated openapi.json
     * @throws IOException
     *             if something went wrong
     */
    public static String generateOpenAPI(Path buildDirectory)
            throws IOException {
        EngineConfiguration engineConfiguration = EngineConfiguration
                .loadDirectory(buildDirectory);
        ParserProcessor parserProcessor = new ParserProcessor(
                engineConfiguration, OpenAPIUtil.class.getClassLoader());
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
