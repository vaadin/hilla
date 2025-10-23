package com.vaadin.hilla.typescript.codegen;

import io.swagger.v3.oas.models.OpenAPI;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main orchestrator for TypeScript code generation. This class manages the
 * execution of TypeScript generator plugins and writes the generated code to
 * files.
 */
public class TypeScriptGenerator {
    private static final Logger logger = LoggerFactory
            .getLogger(TypeScriptGenerator.class);

    private final List<TypeScriptGeneratorPlugin> plugins = new ArrayList<>();
    private final String outputDirectory;

    /**
     * Creates a new TypeScript generator.
     *
     * @param outputDirectory the output directory for generated files
     */
    public TypeScriptGenerator(@NonNull String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Adds a plugin to the generator.
     *
     * @param plugin the plugin to add
     * @return this generator for chaining
     */
    @NonNull
    public TypeScriptGenerator addPlugin(
            @NonNull TypeScriptGeneratorPlugin plugin) {
        plugins.add(plugin);
        return this;
    }

    /**
     * Generates TypeScript code from an OpenAPI specification.
     *
     * @param openAPI the OpenAPI specification
     * @return a map of file paths to generated content
     */
    @NonNull
    public Map<String, String> generate(@NonNull OpenAPI openAPI) {
        GenerationContext context = new GenerationContext(outputDirectory);
        Map<String, String> allGeneratedFiles = new HashMap<>();

        // Sort plugins by order
        List<TypeScriptGeneratorPlugin> sortedPlugins = new ArrayList<>(
                plugins);
        sortedPlugins.sort(Comparator.comparingInt(
                TypeScriptGeneratorPlugin::getOrder));

        // Execute each plugin
        for (TypeScriptGeneratorPlugin plugin : sortedPlugins) {
            logger.debug("Executing plugin: {}", plugin.getName());
            try {
                Map<String, String> generatedFiles = plugin.generate(openAPI,
                        context);
                allGeneratedFiles.putAll(generatedFiles);
                logger.debug("Plugin {} generated {} files", plugin.getName(),
                        generatedFiles.size());
            } catch (Exception e) {
                logger.error("Error executing plugin: {}", plugin.getName(), e);
                throw new TypeScriptGenerationException(
                        "Failed to execute plugin: " + plugin.getName(), e);
            }
        }

        return allGeneratedFiles;
    }

    /**
     * Generates TypeScript code and writes it to files.
     *
     * @param openAPI the OpenAPI specification
     * @throws IOException if an I/O error occurs while writing files
     */
    public void generateAndWrite(@NonNull OpenAPI openAPI) throws IOException {
        Map<String, String> generatedFiles = generate(openAPI);

        // Ensure output directory exists
        Path outputPath = Paths.get(outputDirectory);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        // Write each generated file
        for (Map.Entry<String, String> entry : generatedFiles.entrySet()) {
            Path filePath = outputPath.resolve(entry.getKey());

            // Create parent directories if needed
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            // Write the file
            Files.writeString(filePath, entry.getValue());
            logger.debug("Wrote file: {}", filePath);
        }

        logger.info("Generated {} TypeScript files in {}",
                generatedFiles.size(), outputDirectory);
    }

    /**
     * Gets the list of registered plugins.
     *
     * @return the list of plugins
     */
    @NonNull
    public List<TypeScriptGeneratorPlugin> getPlugins() {
        return new ArrayList<>(plugins);
    }
}
