package com.vaadin.hilla.typescript.codegen;

import io.swagger.v3.oas.models.OpenAPI;
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
     * @param openAPI the OpenAPI specification to generate code from
     * @param context the generation context for sharing state between plugins
     * @return a map of file paths to generated TypeScript content
     */
    @NonNull
    Map<String, String> generate(@NonNull OpenAPI openAPI,
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
