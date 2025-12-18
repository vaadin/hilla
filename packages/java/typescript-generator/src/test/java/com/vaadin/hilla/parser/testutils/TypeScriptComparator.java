/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.parser.testutils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility for comparing generated TypeScript files against expected snapshots.
 */
public class TypeScriptComparator {

    /**
     * Compare expected and actual TypeScript files.
     *
     * Only verifies that expected snapshot files are present and match.
     * Additional generated files (not in snapshots) are allowed and ignored.
     *
     * @param expected
     *            Map of file name to content (expected snapshots)
     * @param actual
     *            Map of file name to content (generated files)
     * @throws AssertionError
     *             if files don't match
     */
    public void compare(Map<String, String> expected,
            Map<String, String> actual) {
        List<String> errors = new ArrayList<>();

        // Check for missing files - expected files MUST be present
        Set<String> expectedFiles = new HashSet<>(expected.keySet());
        Set<String> actualFiles = new HashSet<>(actual.keySet());

        Set<String> missingFiles = new HashSet<>(expectedFiles);
        missingFiles.removeAll(actualFiles);

        if (!missingFiles.isEmpty()) {
            errors.add("Missing expected files: " + missingFiles);
        }

        // Note: We intentionally do NOT check for extra files.
        // Tests only verify specific files they care about (snapshots).
        // Additional generated files (like connect-client.default.ts,
        // endpoints.ts)
        // are allowed and will be ignored if not in the snapshots directory.

        // Compare content of common files
        Set<String> commonFiles = new HashSet<>(expectedFiles);
        commonFiles.retainAll(actualFiles);

        for (String fileName : commonFiles) {
            String expectedContent = expected.get(fileName);
            String actualContent = actual.get(fileName);

            try {
                compareFileContent(fileName, expectedContent, actualContent);
            } catch (AssertionError e) {
                errors.add(e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            StringBuilder message = new StringBuilder(
                    "TypeScript generation mismatch:\n\n");
            for (int i = 0; i < errors.size(); i++) {
                message.append(i + 1).append(". ").append(errors.get(i))
                        .append("\n\n");
            }
            throw new AssertionError(message.toString());
        }
    }

    private void compareFileContent(String fileName, String expected,
            String actual) {
        // Normalize line endings
        expected = normalizeLineEndings(expected);
        actual = normalizeLineEndings(actual);

        if (expected.equals(actual)) {
            return; // Perfect match
        }

        // Split into lines for detailed comparison
        String[] expectedLines = expected.split("\n", -1);
        String[] actualLines = actual.split("\n", -1);

        // Find first difference
        int maxLines = Math.max(expectedLines.length, actualLines.length);
        int firstDiff = -1;
        for (int i = 0; i < maxLines; i++) {
            String expLine = i < expectedLines.length ? expectedLines[i] : null;
            String actLine = i < actualLines.length ? actualLines[i] : null;

            if (!equals(expLine, actLine)) {
                firstDiff = i;
                break;
            }
        }

        // Build error message with context
        StringBuilder error = new StringBuilder();
        error.append("File '").append(fileName).append("' differs\n");
        error.append("Expected ").append(expectedLines.length)
                .append(" lines, got ").append(actualLines.length)
                .append(" lines\n");

        if (firstDiff >= 0) {
            error.append("\nFirst difference at line ").append(firstDiff + 1)
                    .append(":\n");

            // Show context (3 lines before and after)
            int contextStart = Math.max(0, firstDiff - 3);
            int contextEnd = Math.min(maxLines, firstDiff + 4);

            error.append("\nExpected:\n");
            for (int i = contextStart; i < contextEnd; i++) {
                if (i < expectedLines.length) {
                    error.append(String.format("%4d: %s\n", i + 1,
                            expectedLines[i]));
                } else {
                    error.append(String.format("%4d: <missing>\n", i + 1));
                }
            }

            error.append("\nActual:\n");
            for (int i = contextStart; i < contextEnd; i++) {
                if (i < actualLines.length) {
                    error.append(
                            String.format("%4d: %s\n", i + 1, actualLines[i]));
                } else {
                    error.append(String.format("%4d: <missing>\n", i + 1));
                }
            }

            // Highlight the specific difference
            if (firstDiff < expectedLines.length
                    && firstDiff < actualLines.length) {
                error.append("\nLine ").append(firstDiff + 1)
                        .append(" difference:\n");
                error.append("  Expected: ")
                        .append(quote(expectedLines[firstDiff])).append("\n");
                error.append("  Actual:   ")
                        .append(quote(actualLines[firstDiff])).append("\n");
            }
        }

        throw new AssertionError(error.toString());
    }

    private String normalizeLineEndings(String content) {
        return content.replace("\r\n", "\n").replace("\r", "\n");
    }

    private boolean equals(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    private String quote(String s) {
        if (s == null) {
            return "<null>";
        }
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\t", "\\t") + "\"";
    }
}
