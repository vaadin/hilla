package com.vaadin.hilla.parser.testutils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Utility to generate expected TypeScript files for tests.
 * <p>
 * Usage: Run this with the test class name, endpoint class name, and endpoint annotation class name
 * to generate the expected TypeScript files in the test resources directory.
 */
public class ExpectedGenerator {

    public static Map<String, String> generateForTest(
            Class<?> testClass,
            Class<?> endpointClass,
            Class<? extends Annotation> endpointAnnotation) throws Exception {

        var testHelper = new EndToEndTestHelper(testClass);
        try {
            return testHelper
                    .withEndpoints(endpointClass)
                    .withEndpointAnnotations(endpointAnnotation)
                    .generate();
        } finally {
            testHelper.cleanup();
        }
    }

    public static void writeExpectedFiles(
            Class<?> testClass,
            Map<String, String> generated) throws IOException {

        // Find test resources directory
        var testPackage = testClass.getPackageName().replace('.', '/');
        var projectRoot = findProjectRoot(Paths.get(System.getProperty("user.dir")));
        var testResourcesDir = projectRoot
                .resolve("src/test/resources")
                .resolve(testPackage)
                .resolve("expected");

        Files.createDirectories(testResourcesDir);

        // Write endpoint files only (skip utility files)
        for (var entry : generated.entrySet()) {
            var fileName = entry.getKey();
            // Skip connect-client and endpoints.ts files
            if (fileName.equals("connect-client.default.ts") ||
                fileName.equals("endpoints.ts") ||
                fileName.equals("generated-file-list.txt")) {
                continue;
            }

            var outputPath = testResourcesDir.resolve(fileName);
            Files.writeString(outputPath, entry.getValue());
            System.out.println("Written: " + outputPath);
        }
    }

    private static Path findProjectRoot(Path current) {
        while (current != null && current.getParent() != null) {
            // Look for src/test/java directory
            if (Files.exists(current.resolve("src/test/java"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new RuntimeException("Could not find project root");
    }
}
