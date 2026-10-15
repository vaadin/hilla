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

public class BooleanConversionTest extends BaseTypeConversionTest {
    @Test
    public void should_ConvertToBoolean_When_ReceiveTrueOrFalse() {
        assertEqualExpectedValueWhenCallingMethod("revertBoolean", "true",
                "false");
        assertEqualExpectedValueWhenCallingMethod("revertBoolean", "false",
                "true");

        assertEqualExpectedValueWhenCallingMethod("revertBooleanBoxed", "true",
                "false");
        assertEqualExpectedValueWhenCallingMethod("revertBooleanBoxed", "false",
                "true");
    }

    @Test
    public void should_ConvertToBoolean_When_ReceiveTrueOrFalseAsString() {
        assertEqualExpectedValueWhenCallingMethod("revertBoolean", "\"true\"",
                "false");
        assertEqualExpectedValueWhenCallingMethod("revertBoolean", "\"True\"",
                "false");
        assertEqualExpectedValueWhenCallingMethod("revertBoolean", "\"false\"",
                "true");
        assertEqualExpectedValueWhenCallingMethod("revertBoolean", "\"False\"",
                "true");

        assertEqualExpectedValueWhenCallingMethod("revertBooleanBoxed",
                "\"true\"", "false");
        assertEqualExpectedValueWhenCallingMethod("revertBooleanBoxed",
                "\"True\"", "false");
        assertEqualExpectedValueWhenCallingMethod("revertBooleanBoxed",
                "\"false\"", "true");
        assertEqualExpectedValueWhenCallingMethod("revertBooleanBoxed",
                "\"False\"", "true");
    }

    @Test
    public void should_FailToConvertToBoolean_When_ReceiveMixedcaseString() {
        assert400ResponseWhenCallingMethod("revertBoolean", "\"TRue\"");
        assert400ResponseWhenCallingMethod("revertBoolean", "\"FAlse\"");

        assert400ResponseWhenCallingMethod("revertBooleanBoxed", "\"TRue\"");
        assert400ResponseWhenCallingMethod("revertBooleanBoxed", "\"FAlse\"");
    }

    @Test
    public void should_ConvertToBoolean_When_ReceiveUppercaseString() {
        assertEqualExpectedValueWhenCallingMethod("revertBoolean", "\"TRUE\"",
                "false");
        assertEqualExpectedValueWhenCallingMethod("revertBoolean", "\"FALSE\"",
                "true");

        assertEqualExpectedValueWhenCallingMethod("revertBooleanBoxed",
                "\"TRUE\"", "false");
        assertEqualExpectedValueWhenCallingMethod("revertBooleanBoxed",
                "\"FALSE\"", "true");
    }

    @Test
    public void should_ConvertToBoolean_When_ReceiveANumber() {
        assertEqualExpectedValueWhenCallingMethod("revertBoolean", "1",
                "false");
        assertEqualExpectedValueWhenCallingMethod("revertBoolean", "-1",
                "false");
        assertEqualExpectedValueWhenCallingMethod("revertBoolean", "0", "true");

        assertEqualExpectedValueWhenCallingMethod("revertBooleanBoxed", "1",
                "false");
        assertEqualExpectedValueWhenCallingMethod("revertBooleanBoxed", "-1",
                "false");
        assertEqualExpectedValueWhenCallingMethod("revertBooleanBoxed", "0",
                "true");
    }

    @Test
    public void should_FailToConvertToBoolean_When_ReceiveARandomString() {
        assert400ResponseWhenCallingMethod("revertBoolean", "\"foo\"");

        assert400ResponseWhenCallingMethod("revertBooleanBoxed", "\"foo\"");
    }

    @Test
    public void should_FailToConvertToBoolean_When_ReceiveADecimal() {
        assert400ResponseWhenCallingMethod("revertBoolean", "1.1");

        assert400ResponseWhenCallingMethod("revertBooleanBoxed", "1.1");
    }

    @Test
    public void should_HandleSpecialInputForBoolean_When_ReceiveSpecialInput() {
        assertEqualExpectedValueWhenCallingMethod("revertBoolean", "null",
                "true");

        assertEqualExpectedValueWhenCallingMethod("revertBooleanBoxed", "null",
                "null");
    }
}
