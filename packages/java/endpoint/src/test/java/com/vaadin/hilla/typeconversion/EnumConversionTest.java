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

public class EnumConversionTest extends BaseTypeConversionTest {

    @Test
    public void should_ConvertToEnum_When_ReceiveStringWithSameName() {
        String inputValue = "\"FIRST\"";
        String expectedValue = "\"SECOND\"";
        assertEqualExpectedValueWhenCallingMethod("getNextEnum", inputValue,
                expectedValue);
    }

    @Test
    public void should_FailToConvertToEnum_When_ReceiveStringWithWrongName() {
        String inputValue = "\"WRONG_ENUM\"";
        assert400ResponseWhenCallingMethod("getNextEnum", inputValue);

        String someNumberInput = "111";
        assert400ResponseWhenCallingMethod("getNextEnum", someNumberInput);
    }

    @Test
    public void should_FailToConvertToEnum_When_ReceiveStringWithWrongCase() {
        String firstInputValue = "\"first\"";
        String secondInputValue = "\"First\"";
        String thirdInputValue = "\"fIrst\"";
        assert400ResponseWhenCallingMethod("getNextEnum", firstInputValue);
        assert400ResponseWhenCallingMethod("getNextEnum", secondInputValue);
        assert400ResponseWhenCallingMethod("getNextEnum", thirdInputValue);
    }
}
