package com.vaadin.hilla.parser.testutils;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Utilities for asserting TypeScript code equality in tests.
 * <p>
 * These utilities normalize whitespace, line endings, and other
 * formatting differences to focus on semantic equality.
 */
public class TypeScriptAssertions {

    /**
     * Asserts that generated TypeScript matches expected TypeScript.
     * <p>
     * Normalizes whitespace, line endings, and formatting before comparison
     * to avoid false negatives from formatting differences.
     *
     * @param expected expected TypeScript code
     * @param actual   actual TypeScript code
     */
    public static void assertTypeScriptEquals(String expected, String actual) {
        var normalizedExpected = normalize(expected);
        var normalizedActual = normalize(actual);

        if (!normalizedExpected.equals(normalizedActual)) {
            var diff = computeDiff(normalizedExpected, normalizedActual);
            fail(String.format(
                    "TypeScript output mismatch:\n\n" +
                    "=== Expected ===\n%s\n\n" +
                    "=== Actual ===\n%s\n\n" +
                    "=== Diff ===\n%s",
                    expected,
                    actual,
                    diff
            ));
        }
    }

    /**
     * Asserts that generated TypeScript matches expected TypeScript,
     * with a filename for better error messages.
     *
     * @param fileName file name for error reporting
     * @param actual   actual TypeScript code
     * @param expected expected TypeScript code
     */
    public static void assertTypeScriptEquals(String fileName, String actual, String expected) {
        try {
            assertTypeScriptEquals(expected, actual);
        } catch (AssertionError e) {
            throw new AssertionError("File: " + fileName + "\n" + e.getMessage(), e);
        }
    }

    /**
     * Normalize TypeScript code for comparison.
     * <p>
     * This includes:
     * - Normalizing line endings to \n
     * - Removing trailing whitespace
     * - Normalizing multiple blank lines to double newlines
     * - Trimming leading/trailing whitespace
     *
     * @param typescript TypeScript code to normalize
     * @return normalized code
     */
    private static String normalize(String typescript) {
        if (typescript == null) {
            return "";
        }

        return typescript
                // Normalize line endings
                .replaceAll("\\r\\n", "\n")
                .replaceAll("\\r", "\n")
                // Remove trailing whitespace on each line
                .replaceAll("[ \\t]+\n", "\n")
                // Normalize multiple blank lines to maximum 2 newlines
                .replaceAll("\n\n\n+", "\n\n")
                // Trim overall
                .trim();
    }

    /**
     * Compute a simple line-by-line diff for error messages.
     *
     * @param expected normalized expected code
     * @param actual   normalized actual code
     * @return diff description
     */
    private static String computeDiff(String expected, String actual) {
        var expectedLines = expected.split("\n");
        var actualLines = actual.split("\n");

        var diff = new StringBuilder();
        var maxLines = Math.max(expectedLines.length, actualLines.length);
        var diffCount = 0;

        for (int i = 0; i < maxLines; i++) {
            var expectedLine = i < expectedLines.length ? expectedLines[i] : "";
            var actualLine = i < actualLines.length ? actualLines[i] : "";

            if (!expectedLine.equals(actualLine)) {
                diffCount++;
                diff.append(String.format("Line %d:\n", i + 1));
                diff.append(String.format("  - Expected: %s\n", escapeLine(expectedLine)));
                diff.append(String.format("  + Actual:   %s\n", escapeLine(actualLine)));
                diff.append("\n");

                // Limit diff output to first 10 differences
                if (diffCount >= 10) {
                    diff.append(String.format("... and %d more differences\n",
                            countRemainingDifferences(expectedLines, actualLines, i + 1)));
                    break;
                }
            }
        }

        if (diffCount == 0) {
            return "No line differences found (possible character-level difference)";
        }

        return diff.toString();
    }

    private static String escapeLine(String line) {
        if (line.isEmpty()) {
            return "(empty line)";
        }
        // Show whitespace visibly
        return line.replace(" ", "·").replace("\t", "→");
    }

    private static int countRemainingDifferences(String[] expectedLines, String[] actualLines, int startFrom) {
        var count = 0;
        var maxLines = Math.max(expectedLines.length, actualLines.length);

        for (int i = startFrom; i < maxLines; i++) {
            var expectedLine = i < expectedLines.length ? expectedLines[i] : "";
            var actualLine = i < actualLines.length ? actualLines[i] : "";

            if (!expectedLine.equals(actualLine)) {
                count++;
            }
        }

        return count;
    }
}
