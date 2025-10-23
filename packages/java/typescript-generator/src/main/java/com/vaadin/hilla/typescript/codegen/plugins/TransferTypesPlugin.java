package com.vaadin.hilla.typescript.codegen.plugins;

import com.vaadin.hilla.typescript.codegen.GenerationContext;
import com.vaadin.hilla.typescript.codegen.ParserOutput;
import com.vaadin.hilla.typescript.codegen.TypeScriptGeneratorPlugin;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Plugin that handles special transfer types (Flux, Signal, etc.).
 * Currently a placeholder - most type mapping is handled by TypeMapper.
 */
public class TransferTypesPlugin implements TypeScriptGeneratorPlugin {
    private static final Logger logger = LoggerFactory
            .getLogger(TransferTypesPlugin.class);

    @Override
    @NonNull
    public Map<String, String> generate(@NonNull ParserOutput parserOutput,
            @NonNull GenerationContext context) {
        Map<String, String> generatedFiles = new HashMap<>();

        // TODO: Implement transfer types generation if needed
        // Most type mapping is already handled by TypeMapper
        // This could generate type aliases or re-exports for special types
        // For now, this is a placeholder
        logger.debug(
                "TransferTypesPlugin: Skipping generation (not yet implemented)");

        return generatedFiles;
    }

    @Override
    @NonNull
    public String getName() {
        return "TransferTypesPlugin";
    }

    @Override
    public int getOrder() {
        return 5; // Run early, before ModelPlugin
    }
}
