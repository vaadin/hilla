package com.vaadin.hilla.typescript.codegen;

import org.jspecify.annotations.NonNull;

import java.util.Map;

/**
 * Interface for TypeScript code generation plugins. Each plugin is responsible
 * for generating specific types of TypeScript code (e.g., models, clients,
 * barrel files).
 */
public interface TypeScriptGeneratorPlugin {

    /**
     * Executes this plugin to generate TypeScript code.
     *
     * @param parserOutput the parser output containing endpoints and entities
     * @param context      the generation context for sharing state between
     *                     plugins
     * @return a map of file paths to generated TypeScript content
     */
    @NonNull
    Map<String, String> generate(@NonNull ParserOutput parserOutput,
            @NonNull GenerationContext context);

    /**
     * Returns the name of this plugin.
     *
     * @return the plugin name
     */
    @NonNull
    String getName();

    /**
     * Returns the execution order of this plugin. Plugins are executed in
     * ascending order.
     *
     * @return the execution order (lower values execute first)
     */
    default int getOrder() {
        return 100;
    }
}
