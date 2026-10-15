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

public class ShortConversionTest extends BaseTypeConversionTest {
    @Test
    public void should_ConvertToShort_When_ReceiveANumber() {
        assertEqualExpectedValueWhenCallingMethod("addOneShort", "1", "2");
        assertEqualExpectedValueWhenCallingMethod("addOneShort", "0", "1");
        assertEqualExpectedValueWhenCallingMethod("addOneShort", "-1", "0");

        assertEqualExpectedValueWhenCallingMethod("addOneShortBoxed", "1", "2");
        assertEqualExpectedValueWhenCallingMethod("addOneShortBoxed", "0", "1");
        assertEqualExpectedValueWhenCallingMethod("addOneShortBoxed", "-1",
                "0");
    }

    @Test
    public void should_ConvertToShort_When_ReceiveANumberAsString() {
        assertEqualExpectedValueWhenCallingMethod("addOneShort", "\"1\"", "2");
        assertEqualExpectedValueWhenCallingMethod("addOneShort", "\"0\"", "1");
        assertEqualExpectedValueWhenCallingMethod("addOneShort", "\"-1\"", "0");

        assertEqualExpectedValueWhenCallingMethod("addOneShortBoxed", "\"1\"",
                "2");
        assertEqualExpectedValueWhenCallingMethod("addOneShortBoxed", "\"0\"",
                "1");
        assertEqualExpectedValueWhenCallingMethod("addOneShortBoxed", "\"-1\"",
                "0");
    }

    @Test
    public void should_ConvertToShort_When_ReceiveDecimalAsNumber() {
        assertEqualExpectedValueWhenCallingMethod("addOneShort", "1.1", "2");
        assertEqualExpectedValueWhenCallingMethod("addOneShort", "0.0", "1");
        assertEqualExpectedValueWhenCallingMethod("addOneShort", "-1.9", "0");

        assertEqualExpectedValueWhenCallingMethod("addOneShortBoxed", "1.1",
                "2");
        assertEqualExpectedValueWhenCallingMethod("addOneShortBoxed", "0.0",
                "1");
        assertEqualExpectedValueWhenCallingMethod("addOneShortBoxed", "-1.9",
                "0");
    }

    @Test
    public void should_FailToConvertToShort_When_ReceiveDecimalAsString() {
        assert400ResponseWhenCallingMethod("addOneShort", "\"1.1\"");

        assert400ResponseWhenCallingMethod("addOneShortBoxed", "\"1.1\"");
    }

    @Test
    public void should_FailToConvertToShort_When_ReceiveANumberOverflowOrUnderflow() {
        String overflowShort = "32768";
        String underflowShort = "-32769";

        assert400ResponseWhenCallingMethod("addOneShort", overflowShort);
        assert400ResponseWhenCallingMethod("addOneShort", underflowShort);

        assert400ResponseWhenCallingMethod("addOneShortBoxed", overflowShort);
        assert400ResponseWhenCallingMethod("addOneShortBoxed", underflowShort);
    }

    @Test
    public void should_FailToConvertToShort_When_ReceiveANumberOverflowOrUnderflowAsString() {
        String overflowShort = "\"32768\"";
        String underflowShort = "\"-32769\"";

        assert400ResponseWhenCallingMethod("addOneShort", overflowShort);
        assert400ResponseWhenCallingMethod("addOneShort", underflowShort);

        assert400ResponseWhenCallingMethod("addOneShortBoxed", overflowShort);
        assert400ResponseWhenCallingMethod("addOneShortBoxed", underflowShort);
    }
}
