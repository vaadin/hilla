package com.vaadin.hilla.typescript.codegen.plugins;

import com.vaadin.hilla.typescript.codegen.GenerationContext;
import com.vaadin.hilla.typescript.codegen.ParserOutput;
import com.vaadin.hilla.typescript.codegen.TypeScriptGeneratorPlugin;
import com.vaadin.hilla.typescript.codegen.TypeScriptWriter;
import com.vaadin.hilla.typescript.parser.models.ClassInfoModel;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Plugin that generates barrel/index files (endpoints.ts) for clean imports.
 */
public class BarrelPlugin implements TypeScriptGeneratorPlugin {
    private static final Logger logger = LoggerFactory
            .getLogger(BarrelPlugin.class);

    @Override
    @NonNull
    public Map<String, String> generate(@NonNull ParserOutput parserOutput,
            @NonNull GenerationContext context) {
        Map<String, String> generatedFiles = new HashMap<>();

        if (parserOutput.getEndpoints().isEmpty()) {
            logger.debug("No endpoints found for barrel file generation");
            return generatedFiles;
        }

        String barrelContent = generateBarrelFile(parserOutput);
        generatedFiles.put("endpoints.ts", barrelContent);
        logger.debug("Generated barrel file with {} endpoints",
                parserOutput.getEndpoints().size());

        return generatedFiles;
    }

    private String generateBarrelFile(ParserOutput parserOutput) {
        TypeScriptWriter writer = new TypeScriptWriter();

        writer.appendLine("/**");
        writer.appendLine(
                " * Barrel file for all endpoint clients. Import endpoints from this file.");
        writer.appendLine(" */");
        writer.appendBlankLine();

        // Generate exports for each endpoint, sorted alphabetically
        String exports = parserOutput.getEndpoints().stream()
                .map(ClassInfoModel::getSimpleName).sorted()
                .map(this::generateEndpointExport)
                .collect(Collectors.joining("\n"));

        writer.append(exports);

        return writer.build();
    }

    private String generateEndpointExport(String endpointName) {
        // Use template
        String template = "export * as UserEndpoint from './UserEndpoint.js';";

        return template.replace("UserEndpoint", endpointName)
                .replace("'./UserEndpoint.js'", "'./" + endpointName + ".js'");
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
