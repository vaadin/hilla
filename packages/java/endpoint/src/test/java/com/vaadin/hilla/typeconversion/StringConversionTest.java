/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla.typeconversion;

import org.junit.Test;

public class StringConversionTest extends BaseTypeConversionTest {

    @Test
    public void should_ConvertToString_When_ReceiveAString() {
        assertEqualExpectedValueWhenCallingMethod("addFooString",
                "\"some-string\"", "\"some-stringfoo\"");
        assertEqualExpectedValueWhenCallingMethod("addFooString", "\"null\"",
                "\"nullfoo\"");
        assertEqualExpectedValueWhenCallingMethod("addFooString", "null",
                "\"nullfoo\"");
    }

}
