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
 * Plugin that generates React Signals integration for endpoints.
 * Currently a placeholder for future implementation.
 */
public class SignalsPlugin implements TypeScriptGeneratorPlugin {
    private static final Logger logger = LoggerFactory
            .getLogger(SignalsPlugin.class);

    @Override
    @NonNull
    public Map<String, String> generate(@NonNull ParserOutput parserOutput,
            @NonNull GenerationContext context) {
        Map<String, String> generatedFiles = new HashMap<>();

        // TODO: Implement Signals generation
        // This would generate React Signal hooks for endpoints
        // For now, this is a placeholder
        logger.debug(
                "SignalsPlugin: Skipping generation (not yet implemented)");

        return generatedFiles;
    }

    @Override
    @NonNull
    public String getName() {
        return "SignalsPlugin";
    }

    @Override
    public int getOrder() {
        return 40; // Run after PushPlugin
    }
}
