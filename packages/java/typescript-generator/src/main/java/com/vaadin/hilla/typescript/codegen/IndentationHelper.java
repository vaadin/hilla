package com.vaadin.hilla.typescript.codegen;

import org.jspecify.annotations.NonNull;

/**
 * Utility class for managing indentation in generated TypeScript code.
 */
public final class IndentationHelper {
    private static final String DEFAULT_INDENT = "  "; // 2 spaces

    private IndentationHelper() {
        // Utility class
    }

    /**
     * Indents text (single or multiple lines) by the specified level.
     *
     * @param text
     *            the text to indent
     * @param level
     *            the indentation level (number of indent units)
     * @return the indented text
     */
    @NonNull
    public static String indent(@NonNull String text, int level) {
        if (text.isEmpty()) {
            return text;
        }

        String indentStr = DEFAULT_INDENT.repeat(Math.max(0, level));

        // Check if it's multiline
        if (text.contains("\n")) {
            return text.lines()
                    .map(line -> line.isEmpty() ? line : indentStr + line)
                    .collect(java.util.stream.Collectors.joining("\n"));
        } else {
            return indentStr + text;
        }
    }

    /**
     * Creates an indentation string for the specified level.
     *
     * @param level
     *            the indentation level
     * @return the indentation string
     */
    @NonNull
    public static String getIndent(int level) {
        return DEFAULT_INDENT.repeat(Math.max(0, level));
    }
}
