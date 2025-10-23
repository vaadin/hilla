package com.vaadin.hilla.typescript.codegen.plugins;

import com.vaadin.hilla.typescript.codegen.GenerationContext;
import com.vaadin.hilla.typescript.codegen.TypeScriptGeneratorPlugin;
import com.vaadin.hilla.typescript.codegen.TypeScriptWriter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Plugin that generates TypeScript interfaces from OpenAPI schemas.
 */
public class ModelPlugin implements TypeScriptGeneratorPlugin {
    private static final Logger logger = LoggerFactory
            .getLogger(ModelPlugin.class);

    @Override
    @NonNull
    public Map<String, String> generate(@NonNull OpenAPI openAPI,
            @NonNull GenerationContext context) {
        Map<String, String> generatedFiles = new HashMap<>();

        if (openAPI.getComponents() == null
                || openAPI.getComponents().getSchemas() == null) {
            logger.debug("No schemas found in OpenAPI specification");
            return generatedFiles;
        }

        Map<String, Schema> schemas = openAPI.getComponents().getSchemas();

        for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
            String schemaName = entry.getKey();
            Schema schema = entry.getValue();

            try {
                String typeScriptCode = generateInterface(schemaName, schema);
                String fileName = schemaName + ".ts";
                generatedFiles.put(fileName, typeScriptCode);
                logger.debug("Generated interface for schema: {}", schemaName);
            } catch (Exception e) {
                logger.error("Failed to generate interface for schema: {}",
                        schemaName, e);
            }
        }

        return generatedFiles;
    }

    private String generateInterface(String name, Schema schema) {
        TypeScriptWriter writer = new TypeScriptWriter();

        // Generate properties
        String properties = generateProperties(schema);

        // Use template with replacements
        String template = """
                export interface Person {
                  name: string;
                  age: number;
                }
                """;

        String code = template.replace("Person", name).replace(
                "  name: string;\n  age: number;", properties);

        writer.append(code);
        return writer.build();
    }

    private String generateProperties(Schema schema) {
        if (schema.getProperties() == null
                || schema.getProperties().isEmpty()) {
            return "";
        }

        Map<String, Schema> properties = schema.getProperties();
        return properties.entrySet().stream()
                .map(entry -> generateProperty((String) entry.getKey(),
                        (Schema) entry.getValue(), schema))
                .collect(Collectors.joining("\n"));
    }

    private String generateProperty(String propertyName, Schema propertySchema,
            Schema parentSchema) {
        String typeName = mapSchemaToTypeScript(propertySchema);
        boolean isRequired = isRequired(propertyName, parentSchema);

        if (isRequired) {
            return "  " + propertyName + ": " + typeName + ";";
        } else {
            return "  " + propertyName + "?: " + typeName + ";";
        }
    }

    private boolean isRequired(String propertyName, Schema parentSchema) {
        return parentSchema.getRequired() != null
                && parentSchema.getRequired().contains(propertyName);
    }

    private String mapSchemaToTypeScript(Schema schema) {
        if (schema.get$ref() != null) {
            // Extract type name from reference like
            // "#/components/schemas/Person"
            String ref = schema.get$ref();
            int lastSlash = ref.lastIndexOf('/');
            return lastSlash >= 0 ? ref.substring(lastSlash + 1) : ref;
        }

        String type = schema.getType();
        if (type == null) {
            return "any";
        }

        return switch (type) {
        case "string" -> "string";
        case "number", "integer" -> "number";
        case "boolean" -> "boolean";
        case "array" -> {
            Schema items = (Schema) schema.getItems();
            if (items != null) {
                yield mapSchemaToTypeScript(items) + "[]";
            }
            yield "any[]";
        }
        case "object" -> {
            // Could be a map or a plain object
            if (schema.getAdditionalProperties() != null) {
                Object additionalProps = schema.getAdditionalProperties();
                if (additionalProps instanceof Schema) {
                    String valueType = mapSchemaToTypeScript(
                            (Schema) additionalProps);
                    yield "Record<string, " + valueType + ">";
                }
            }
            yield "Record<string, any>";
        }
        default -> "any";
        };
    }

    @Override
    @NonNull
    public String getName() {
        return "ModelPlugin";
    }

    @Override
    public int getOrder() {
        return 10; // Run early to generate types used by other plugins
    }
}
