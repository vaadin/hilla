package com.vaadin.hilla.parser.testutils;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.vaadin.hilla.parser.utils.JsonPrinter;

public final class JsonAssertions {
    private final static JsonPrinter printer = new JsonPrinter();

    public static void assertEquals(Object expected, Object actual)
            throws JsonProcessingException {
        try {
            String expectedJson = printer.writeAsString(expected);
            String actualJson = printer.writeAsString(actual);

            // Use JSONAssert for comparison - ignores whitespace and provides
            // precise diffs
            JSONAssert.assertEquals(expectedJson, actualJson,
                    JSONCompareMode.STRICT);
        } catch (JSONException | AssertionError e) {
            // Enhance error message with more context around the differing
            // field
            String originalMessage = e.getMessage();
            String enhancedMessage = enhanceErrorMessage(originalMessage,
                    expected, actual);
            throw new AssertionError(enhancedMessage, e);
        }
    }

    private static String enhanceErrorMessage(String originalMessage,
            Object expected, Object actual) throws JsonProcessingException {
        StringBuilder enhanced = new StringBuilder();
        enhanced.append("JSON comparison failed:\n");
        enhanced.append(originalMessage);
        enhanced.append(
                "\n\nFor better context, here are the complete JSON structures:\n\n");
        enhanced.append("Expected:\n");
        enhanced.append(printer.pretty().writeAsString(expected));
        enhanced.append("\n\nActual:\n");
        enhanced.append(printer.pretty().writeAsString(actual));
        return enhanced.toString();
    }
}
