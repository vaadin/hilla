package com.vaadin.hilla.typescript.codegen.plugins;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.vaadin.hilla.typescript.codegen.GenerationContext;
import com.vaadin.hilla.typescript.codegen.ParserOutput;
import com.vaadin.hilla.typescript.codegen.TypeScriptGeneratorPlugin;
import com.vaadin.hilla.typescript.codegen.TypeScriptWriter;
import com.vaadin.hilla.typescript.parser.models.ClassInfoModel;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Plugin that generates TypeScript union types and type guards for polymorphic
 * types annotated with @JsonSubTypes.
 */
public class SubtypesPlugin implements TypeScriptGeneratorPlugin {
    private static final Logger logger = LoggerFactory
            .getLogger(SubtypesPlugin.class);

    @Override
    @NonNull
    public Map<String, String> generate(@NonNull ParserOutput parserOutput,
            @NonNull GenerationContext context) {
        Map<String, String> generatedFiles = new HashMap<>();

        // Find all classes with @JsonSubTypes annotation in both endpoints and
        // entities
        List<ClassInfoModel> allClasses = parserOutput.getAllClasses();

        for (ClassInfoModel classInfo : allClasses) {
            try {
                // Try to load the class to check for @JsonSubTypes annotation
                Class<?> cls = Class.forName(classInfo.getName());

                // Check if class has @JsonSubTypes annotation
                JsonSubTypes jsonSubTypes = cls
                        .getAnnotation(JsonSubTypes.class);
                if (jsonSubTypes != null && jsonSubTypes.value().length > 0) {
                    String subtypesCode = generateSubtypesFile(classInfo,
                            jsonSubTypes);
                    String fileName = classInfo.getSimpleName() + "Subtypes.ts";
                    generatedFiles.put(fileName, subtypesCode);
                    logger.debug("Generated subtypes file for: {}",
                            classInfo.getSimpleName());
                }
            } catch (ClassNotFoundException e) {
                // Class not found, skip
                logger.debug("Could not load class for annotation check: {}",
                        classInfo.getName());
            } catch (Exception e) {
                logger.error("Failed to generate subtypes for: {}",
                        classInfo.getSimpleName(), e);
            }
        }

        return generatedFiles;
    }

    private String generateSubtypesFile(ClassInfoModel baseClass,
            JsonSubTypes jsonSubTypes) {
        TypeScriptWriter writer = new TypeScriptWriter();

        // Get subtype information
        List<SubtypeInfo> subtypes = Arrays.stream(jsonSubTypes.value())
                .map(type -> new SubtypeInfo(type.value().getSimpleName(),
                        type.name()))
                .collect(Collectors.toList());

        // Generate union type
        String unionType = generateUnionType(baseClass.getSimpleName(),
                subtypes);

        // Generate type guard functions
        String typeGuards = subtypes.stream()
                .map(subtype -> generateTypeGuard(subtype))
                .collect(Collectors.joining("\n\n"));

        writer.appendLine("/**");
        writer.appendLine(" * Polymorphic type definitions for "
                + baseClass.getSimpleName());
        writer.appendLine(" */");
        writer.appendBlankLine();
        writer.append(unionType);
        writer.appendBlankLine();
        writer.appendBlankLine();
        writer.append(typeGuards);

        return writer.build();
    }

    private String generateUnionType(String baseName,
            List<SubtypeInfo> subtypes) {
        String subtypeNames = subtypes.stream().map(s -> s.className)
                .collect(Collectors.joining(" | "));

        return String.format("export type %s = %s;", baseName, subtypeNames);
    }

    private String generateTypeGuard(SubtypeInfo subtype) {
        // Template for type guard function
        String template = """
                export function isOrFilter(obj: any): obj is OrFilter {
                  return obj != null && obj['@type'] === 'or';
                }
                """;

        String functionName = "is" + subtype.className;

        return template.replace("isOrFilter", functionName)
                .replace("OrFilter", subtype.className)
                .replace("'or'", "'" + subtype.typeName + "'");
    }

    private static class SubtypeInfo {
        final String className;
        final String typeName;

        SubtypeInfo(String className, String typeName) {
            this.className = className;
            this.typeName = typeName;
        }
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
