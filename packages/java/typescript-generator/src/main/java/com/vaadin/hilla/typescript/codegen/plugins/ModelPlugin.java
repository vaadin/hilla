package com.vaadin.hilla.typescript.codegen.plugins;

import com.vaadin.hilla.typescript.codegen.GenerationContext;
import com.vaadin.hilla.typescript.codegen.ParserOutput;
import com.vaadin.hilla.typescript.codegen.TypeMapper;
import com.vaadin.hilla.typescript.codegen.TypeScriptGeneratorPlugin;
import com.vaadin.hilla.typescript.codegen.TypeScriptWriter;
import com.vaadin.hilla.typescript.parser.models.ClassInfoModel;
import com.vaadin.hilla.typescript.parser.models.FieldInfoModel;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Plugin that generates TypeScript interfaces from Java entity classes.
 */
public class ModelPlugin implements TypeScriptGeneratorPlugin {
    private static final Logger logger = LoggerFactory
            .getLogger(ModelPlugin.class);

    @Override
    @NonNull
    public Map<String, String> generate(@NonNull ParserOutput parserOutput,
            @NonNull GenerationContext context) {
        Map<String, String> generatedFiles = new HashMap<>();

        // Generate interfaces for all entities (and endpoints if needed)
        for (ClassInfoModel classInfo : parserOutput.getAllClasses()) {
            try {
                String typeScriptCode = generateInterface(classInfo);
                String fileName = classInfo.getSimpleName() + ".ts";
                generatedFiles.put(fileName, typeScriptCode);
                logger.debug("Generated interface for class: {}",
                        classInfo.getSimpleName());
            } catch (Exception e) {
                logger.error("Failed to generate interface for class: {}",
                        classInfo.getSimpleName(), e);
            }
        }

        return generatedFiles;
    }

    private String generateInterface(ClassInfoModel classInfo) {
        TypeScriptWriter writer = new TypeScriptWriter();

        // Generate properties from fields
        String properties = generateProperties(classInfo);

        // Use template with replacements
        String template = """
                export interface Person {
                  name: string;
                  age: number;
                }
                """;

        String className = classInfo.getSimpleName();
        String code = template.replace("Person", className).replace(
                "  name: string;\n  age: number;", properties);

        writer.append(code);
        return writer.build();
    }

    private String generateProperties(ClassInfoModel classInfo) {
        if (classInfo.getFields() == null
                || classInfo.getFields().isEmpty()) {
            return "  // No properties";
        }

        return classInfo.getFields().stream().map(this::generateProperty)
                .collect(Collectors.joining("\n"));
    }

    private String generateProperty(FieldInfoModel field) {
        String fieldName = field.getName();
        String typeName = TypeMapper.toTypeScript(field.getType());

        // For now, make all properties optional
        // TODO: Detect @NonNull or required fields
        return "  " + fieldName + "?: " + typeName + ";";
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
