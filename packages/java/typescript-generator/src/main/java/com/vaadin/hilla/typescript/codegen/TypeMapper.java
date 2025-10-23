package com.vaadin.hilla.typescript.codegen;

import com.vaadin.hilla.typescript.parser.models.SignatureModel;
import com.vaadin.hilla.typescript.parser.models.ArraySignatureModel;
import com.vaadin.hilla.typescript.parser.models.ClassRefSignatureModel;
import org.jspecify.annotations.NonNull;

/**
 * Utility class for mapping Java types to TypeScript types.
 */
public final class TypeMapper {

    private TypeMapper() {
        // Utility class
    }

    /**
     * Maps a Java type signature to a TypeScript type string.
     *
     * @param signature the Java type signature from the parser
     * @return the TypeScript type string
     */
    @NonNull
    public static String toTypeScript(@NonNull SignatureModel signature) {
        // Handle arrays
        if (signature instanceof ArraySignatureModel) {
            ArraySignatureModel arraySignature = (ArraySignatureModel) signature;
            return toTypeScript(arraySignature.getNestedType()) + "[]";
        }

        // Handle class references
        if (signature instanceof ClassRefSignatureModel) {
            ClassRefSignatureModel classRef = (ClassRefSignatureModel) signature;
            return mapClassToTypeScript(classRef);
        }

        // Default to any
        return "any";
    }

    private static String mapClassToTypeScript(
            ClassRefSignatureModel classRef) {
        String className = classRef.getClassInfo().getSimpleName();
        String fullName = classRef.getClassInfo().getName();

        // Map Java primitives and common types to TypeScript
        return switch (fullName) {
        case "java.lang.String" -> "string";
        case "java.lang.Boolean", "boolean" -> "boolean";
        case "java.lang.Integer", "int", "java.lang.Long", "long",
                "java.lang.Short", "short", "java.lang.Byte", "byte",
                "java.lang.Double", "double", "java.lang.Float", "float" ->
            "number";
        case "java.lang.Void", "void" -> "void";
        case "java.util.List", "java.util.Set", "java.util.Collection" -> {
            // Get generic type if available
            if (!classRef.getTypeArguments().isEmpty()) {
                String itemType = toTypeScript(
                        classRef.getTypeArguments().get(0));
                yield itemType + "[]";
            }
            yield "any[]";
        }
        case "java.util.Map" -> {
            // Map<K, V> → Record<K, V>
            if (classRef.getTypeArguments().size() >= 2) {
                String keyType = toTypeScript(
                        classRef.getTypeArguments().get(0));
                String valueType = toTypeScript(
                        classRef.getTypeArguments().get(1));
                yield "Record<" + keyType + ", " + valueType + ">";
            }
            yield "Record<string, any>";
        }
        case "java.util.Optional" -> {
            // Optional<T> → T | undefined
            if (!classRef.getTypeArguments().isEmpty()) {
                String innerType = toTypeScript(
                        classRef.getTypeArguments().get(0));
                yield innerType + " | undefined";
            }
            yield "any | undefined";
        }
        case "java.time.LocalDate", "java.time.LocalDateTime",
                "java.time.Instant", "java.util.Date" -> "string"; // ISO
                                                                     // strings
        default -> className; // Use simple class name for custom types
        };
    }
}
