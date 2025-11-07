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

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import tools.jackson.core.JacksonException;

import com.vaadin.hilla.parser.utils.JsonPrinter;

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
