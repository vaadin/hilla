/*
 * Copyright 2000-2020 Vaadin Ltd.
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
package com.vaadin.flow.server.connect.typeconversion;

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
