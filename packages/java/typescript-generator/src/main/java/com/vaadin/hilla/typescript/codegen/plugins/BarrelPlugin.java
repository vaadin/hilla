package com.vaadin.hilla.typescript.codegen.plugins;

import com.vaadin.hilla.typescript.codegen.GenerationContext;
import com.vaadin.hilla.typescript.codegen.TypeScriptGeneratorPlugin;
import com.vaadin.hilla.typescript.codegen.TypeScriptWriter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Plugin that generates barrel/index files (endpoints.ts) for clean imports.
 */
public class BarrelPlugin implements TypeScriptGeneratorPlugin {
    private static final Logger logger = LoggerFactory
            .getLogger(BarrelPlugin.class);

    @Override
    @NonNull
    public Map<String, String> generate(@NonNull OpenAPI openAPI,
            @NonNull GenerationContext context) {
        Map<String, String> generatedFiles = new HashMap<>();

        // Extract all endpoint names from tags
        Set<String> endpointNames = new HashSet<>();

        if (openAPI.getTags() != null) {
            endpointNames.addAll(openAPI.getTags().stream().map(Tag::getName)
                    .collect(Collectors.toSet()));
        }

        // Also extract from paths
        if (openAPI.getPaths() != null) {
            openAPI.getPaths().values().forEach(pathItem -> {
                if (pathItem.getPost() != null
                        && pathItem.getPost().getTags() != null) {
                    endpointNames.addAll(pathItem.getPost().getTags());
                }
                if (pathItem.getGet() != null
                        && pathItem.getGet().getTags() != null) {
                    endpointNames.addAll(pathItem.getGet().getTags());
                }
            });
        }

        if (endpointNames.isEmpty()) {
            logger.debug("No endpoints found for barrel file generation");
            return generatedFiles;
        }

        String barrelContent = generateBarrelFile(endpointNames);
        generatedFiles.put("endpoints.ts", barrelContent);
        logger.debug("Generated barrel file with {} endpoints",
                endpointNames.size());

        return generatedFiles;
    }

    private String generateBarrelFile(Set<String> endpointNames) {
        TypeScriptWriter writer = new TypeScriptWriter();

        writer.appendLine("/**");
        writer.appendLine(
                " * Barrel file for all endpoint clients. Import endpoints from this file.");
        writer.appendLine(" */");
        writer.appendBlankLine();

        // Generate exports for each endpoint
        String exports = endpointNames.stream().sorted()
                .map(this::generateEndpointExport)
                .collect(Collectors.joining("\n"));

        writer.append(exports);

        return writer.build();
    }

    private String generateEndpointExport(String endpointName) {
        // Use template
        String template = "export * as UserEndpoint from './UserEndpoint.js';";

        return template.replace("UserEndpoint", endpointName).replace(
                "'./UserEndpoint.js'", "'./" + endpointName + ".js'");
    }

    @Override
    @NonNull
    public String getName() {
        return "BarrelPlugin";
    }

    @Override
    public int getOrder() {
        return 50; // Run after client generation
    }
}
