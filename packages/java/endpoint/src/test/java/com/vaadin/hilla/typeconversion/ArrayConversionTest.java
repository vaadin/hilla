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

public class ArrayConversionTest extends BaseTypeConversionTest {

    @Test
    public void should_ConvertToArrayInt_When_ReceiveArrayInt() {
        String inputArray = "[1,2,3]";
        String expectedArray = "[2,3,4]";
        assertEqualExpectedValueWhenCallingMethod("getAddOneArray", inputArray,
                expectedArray);
    }

    @Test
    public void should_FailToConvertToArrayInt_When_ReceiveMixedIntStringArray() {
        String inputArray = "[1,\"string-value\",2,3]";
        assert400ResponseWhenCallingMethod("getAddOneArray", inputArray);
    }

    @Test
    public void should_ConvertToArrayInt_When_ReceiveMixedNumberArray() {
        String inputArray = "[1,2.0,-3.75]";
        String expectedArray = "[2,3,-2]";
        assertEqualExpectedValueWhenCallingMethod("getAddOneArray", inputArray,
                expectedArray);
    }

    @Test
    public void should_ConvertToArrayObject_When_ReceiveMixedArray() {
        String inputArray = "[1,2.0,-3.75,\"MyString\",[1,2,3]]";
        String expectedArray = "[1,2.0,-3.75,\"MyString\",[1,2,3]]";
        assertEqualExpectedValueWhenCallingMethod("getObjectArray", inputArray,
                expectedArray);
    }

    @Test
    public void should_ConvertToArrayString_When_ReceiveMixedStringNumberArray() {
        String inputArray = "[1,\"string-value\",2.0,3]";
        String expectedArray = "[\"1-foo\",\"string-value-foo\",\"2.0-foo\",\"3-foo\"]";
        assertEqualExpectedValueWhenCallingMethod("getFooStringArray",
                inputArray, expectedArray);
    }
}
