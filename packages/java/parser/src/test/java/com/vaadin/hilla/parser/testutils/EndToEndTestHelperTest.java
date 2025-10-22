package com.vaadin.hilla.parser.testutils;

import static com.vaadin.hilla.parser.testutils.TypeScriptAssertions.assertTypeScriptEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for EndToEndTestHelper to ensure it can run the full pipeline.
 */
public class EndToEndTestHelperTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface TestEndpoint {
    }

    @TestEndpoint
    public static class SimpleEndpoint {
        public String hello(String name) {
            return "Hello, " + name;
        }

        public int add(int a, int b) {
            return a + b;
        }
    }

    private EndToEndTestHelper helper;

    @BeforeEach
    public void setUp() throws Exception {
        helper = new EndToEndTestHelper(getClass());
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (helper != null) {
            helper.cleanup();
        }
    }

    @Test
    public void testGenerate_ShouldProduceTypeScriptFiles() throws Exception {
        // Generate TypeScript from endpoint
        var generated = helper
                .withEndpoints(SimpleEndpoint.class)
                .withEndpointAnnotations(TestEndpoint.class)
                .generate();

        // Should have generated files
        assertFalse(generated.isEmpty(), "Should generate TypeScript files");

        // Should have endpoint file
        var endpointFile = generated.get("SimpleEndpoint.ts");
        assertNotNull(endpointFile, "Should generate SimpleEndpoint.ts");

        // Should contain expected methods
        assertTrue(endpointFile.contains("hello"), "Should contain hello method");
        assertTrue(endpointFile.contains("add"), "Should contain add method");

        // Should have client import
        assertTrue(endpointFile.contains("import"), "Should have imports");
        assertTrue(endpointFile.contains("client"), "Should import client");

        // Should export methods
        assertTrue(endpointFile.contains("export"), "Should export methods");
    }

    @Test
    public void testGenerate_ShouldMatchExpectedFormat() throws Exception {
        var generated = helper
                .withEndpoints(SimpleEndpoint.class)
                .withEndpointAnnotations(TestEndpoint.class)
                .generate();

        var endpointFile = generated.get("SimpleEndpoint.ts");
        assertNotNull(endpointFile);

        // Verify basic structure (not exact match since we don't have expected file yet)
        var lines = endpointFile.split("\n");
        var hasImport = false;
        var hasFunction = false;
        var hasExport = false;

        for (var line : lines) {
            if (line.contains("import") && line.contains("client")) {
                hasImport = true;
            }
            if (line.contains("function") && (line.contains("hello") || line.contains("add"))) {
                hasFunction = true;
            }
            if (line.contains("export") && line.contains("{")) {
                hasExport = true;
            }
        }

        assertTrue(hasImport, "Should have client import");
        assertTrue(hasFunction, "Should have function declarations");
        assertTrue(hasExport, "Should have export statement");
    }

    @Test
    public void testTypeScriptAssertions_ShouldMatchIdenticalCode() {
        var code1 = "function foo() {\n  return 42;\n}";
        var code2 = "function foo() {\n  return 42;\n}";

        // Should not throw
        assertTypeScriptEquals(code1, code2);
    }

    @Test
    public void testTypeScriptAssertions_ShouldNormalizeWhitespace() {
        var code1 = "function foo() {\n  return 42;\n}";
        var code2 = "function foo() {\r\n  return 42;\r\n}"; // Different line endings
        var code3 = "function foo() {\n  return 42;  \n}"; // Trailing space

        // Should all match after normalization
        assertTypeScriptEquals(code1, code2);
        assertTypeScriptEquals(code1, code3);
    }

    @Test
    public void testTypeScriptAssertions_ShouldNormalizeBlankLines() {
        var code1 = "import foo;\n\nfunction bar() {}\n\nexport { bar };";
        var code2 = "import foo;\n\n\n\nfunction bar() {}\n\n\n\nexport { bar };"; // Extra blank lines

        // Should match after normalization
        assertTypeScriptEquals(code1, code2);
    }
}
