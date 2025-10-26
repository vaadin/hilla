package com.vaadin.hilla.typescript.codegen;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for writing TypeScript code using template-based generation.
 * This class helps build TypeScript code by providing utilities for managing
 * imports, exports, and code blocks.
 */
public class TypeScriptWriter {
    private final List<String> imports = new ArrayList<>();
    private final List<String> exports = new ArrayList<>();
    private final StringBuilder content = new StringBuilder();

    /**
     * Adds an import statement.
     *
     * @param importStatement
     *            the import statement (e.g., "import { Foo } from './Bar';")
     * @return this writer for chaining
     */
    @NonNull
    public TypeScriptWriter addImport(@NonNull String importStatement) {
        if (!imports.contains(importStatement)) {
            imports.add(importStatement);
        }
        return this;
    }

    /**
     * Adds a named import from a module.
     *
     * @param names
     *            the names to import
     * @param modulePath
     *            the module path
     * @return this writer for chaining
     */
    @NonNull
    public TypeScriptWriter addNamedImport(@NonNull List<String> names,
            @NonNull String modulePath) {
        if (names.isEmpty()) {
            return this;
        }
        String importStatement = "import { " + String.join(", ", names)
                + " } from '" + modulePath + "';";
        return addImport(importStatement);
    }

    /**
     * Adds a default import from a module.
     *
     * @param name
     *            the default import name
     * @param modulePath
     *            the module path
     * @return this writer for chaining
     */
    @NonNull
    public TypeScriptWriter addDefaultImport(@NonNull String name,
            @NonNull String modulePath) {
        String importStatement = "import " + name + " from '" + modulePath
                + "';";
        return addImport(importStatement);
    }

    /**
     * Adds a type-only import from a module.
     *
     * @param names
     *            the type names to import
     * @param modulePath
     *            the module path
     * @return this writer for chaining
     */
    @NonNull
    public TypeScriptWriter addTypeImport(@NonNull List<String> names,
            @NonNull String modulePath) {
        if (names.isEmpty()) {
            return this;
        }
        String importStatement = "import type { " + String.join(", ", names)
                + " } from '" + modulePath + "';";
        return addImport(importStatement);
    }

    /**
     * Adds an export statement.
     *
     * @param exportStatement
     *            the export statement
     * @return this writer for chaining
     */
    @NonNull
    public TypeScriptWriter addExport(@NonNull String exportStatement) {
        exports.add(exportStatement);
        return this;
    }

    /**
     * Appends content to the writer.
     *
     * @param code
     *            the code to append
     * @return this writer for chaining
     */
    @NonNull
    public TypeScriptWriter append(@NonNull String code) {
        content.append(code);
        return this;
    }

    /**
     * Appends content followed by a newline.
     *
     * @param code
     *            the code to append
     * @return this writer for chaining
     */
    @NonNull
    public TypeScriptWriter appendLine(@NonNull String code) {
        content.append(code).append("\n");
        return this;
    }

    /**
     * Appends a blank line.
     *
     * @return this writer for chaining
     */
    @NonNull
    public TypeScriptWriter appendBlankLine() {
        content.append("\n");
        return this;
    }

    /**
     * Builds the final TypeScript code with imports, content, and exports.
     *
     * @return the complete TypeScript code
     */
    @NonNull
    public String build() {
        StringBuilder result = new StringBuilder();

        // Add imports
        if (!imports.isEmpty()) {
            for (String imp : imports) {
                result.append(imp).append("\n");
            }
            result.append("\n");
        }

        // Add main content
        result.append(content);

        // Add exports if any
        if (!exports.isEmpty()) {
            if (content.length() > 0) {
                result.append("\n");
            }
            for (String export : exports) {
                result.append(export).append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Clears all content, imports, and exports.
     */
    public void clear() {
        imports.clear();
        exports.clear();
        content.setLength(0);
    }
}
