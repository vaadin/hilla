/*
 * Copyright 2000-2021 Vaadin Ltd.
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

package com.vaadin.flow.server.connect;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class EndpointNameCheckerTest {
    private final EndpointNameChecker checker = new EndpointNameChecker();

    @Test
    public void should_ReturnValidationError_When_NullAsEndpointNameGiven() {
        String validationResult = checker.check(null);

        assertNotNull(validationResult);
        assertTrue(validationResult.contains("blank"));
    }

    @Test
    public void should_ReturnValidationError_When_EmptyAsEndpointNameGiven() {
        String validationResult = checker.check("");

        assertNotNull(validationResult);
        assertTrue(validationResult.contains("blank"));
    }

    @Test
    public void should_ReturnValidationError_When_ReservedWordAsEndpointNameGiven() {
        for (String reservedEsWord : EndpointNameChecker.ECMA_SCRIPT_RESERVED_WORDS) {
            String validationResult = checker.check(reservedEsWord);

            assertNotNull(String.format(
                    "Word '%s' should cause validation errors, but it did not",
                    reservedEsWord), validationResult);
            assertTrue(validationResult.contains("reserved"));
        }
    }

    @Test
    public void should_ReturnValidationError_When_EndpointNameWithWhitespaceGiven() {
        for (String wordWithWhitespace : Arrays.asList("bad name", "bad\nname",
                "bad\tname", "bad\rname")) {
            String validationResult = checker.check(wordWithWhitespace);

            assertNotNull(String.format(
                    "Word '%s' should cause validation errors, but it did not",
                    wordWithWhitespace), validationResult);
            assertTrue(validationResult.contains("whitespace"));
        }
    }

    @Test
    public void should_ReturnNoErrors_When_ValidEndpointNameGiven() {
        String validationResult = checker.check("ordinaryName");

        assertNull(validationResult);
    }
}
