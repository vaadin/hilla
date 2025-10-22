package com.vaadin.hilla.typescript.parser.testutils;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import tools.jackson.core.JacksonException;

import com.vaadin.hilla.typescript.parser.utils.JsonPrinter;

public final class JsonAssertions {
    private final static JsonPrinter printer = new JsonPrinter();

    public static void assertEquals(Object expected, Object actual)
            throws JacksonException {
        String expectedJson = printer.writeAsString(expected);
        String actualJson = printer.writeAsString(actual);
        try {
            // Use JSONAssert for comparison - ignores whitespace and provides
            // precise diffs
            JSONAssert.assertEquals(expectedJson, actualJson,
                    JSONCompareMode.STRICT);
        } catch (JSONException | AssertionError e) {
            // Enhance error message with more context around the differing
            // field
            String originalMessage = e.getMessage();
            String enhancedMessage = enhanceErrorMessage(originalMessage,
                    expectedJson, actualJson);
            throw new AssertionError(enhancedMessage, e);
        }
    }

    private static String enhanceErrorMessage(String originalMessage,
            String expectedJson, String actualJson) throws JacksonException {
        StringBuilder enhanced = new StringBuilder();
        enhanced.append("JSON comparison failed:\n");
        enhanced.append(originalMessage);
        enhanced.append(
                "\n\nFor better context, here are the complete JSON structures:\n\n");
        enhanced.append("Expected:\n");
        enhanced.append(expectedJson);
        enhanced.append("\n\nActual:\n");
        enhanced.append(actualJson);
        return enhanced.toString();
    }
}
