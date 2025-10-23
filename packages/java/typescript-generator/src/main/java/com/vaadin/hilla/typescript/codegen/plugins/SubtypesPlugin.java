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
 * Plugin that generates TypeScript type guards for polymorphic types.
 * Currently a placeholder for future implementation.
 */
public class SubtypesPlugin implements TypeScriptGeneratorPlugin {
    private static final Logger logger = LoggerFactory
            .getLogger(SubtypesPlugin.class);

    @Override
    @NonNull
    public Map<String, String> generate(@NonNull ParserOutput parserOutput,
            @NonNull GenerationContext context) {
        Map<String, String> generatedFiles = new HashMap<>();

        // TODO: Implement subtype/polymorphic type guard generation
        // This would generate type guards like:
        // export function isAnimal(obj: any): obj is Animal { ... }
        // For now, this is a placeholder
        logger.debug(
                "SubtypesPlugin: Skipping generation (not yet implemented)");

        return generatedFiles;
    }

    @Override
    @NonNull
    public String getName() {
        return "SubtypesPlugin";
    }

    @Override
    public int getOrder() {
        return 15; // Run after ModelPlugin but before ClientPlugin
    }
}
