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
package com.vaadin.flow.server.connect.typeconversion;

import org.junit.Test;

public class ByteConversionTest extends BaseTypeConversionTest {
    @Test
    public void should_ConvertNumberToByte_When_ReceiveNumber() {
        assertEqualExpectedValueWhenCallingMethod("addOneByte", "1", "2");
        assertEqualExpectedValueWhenCallingMethod("addOneByte", "0", "1");
        assertEqualExpectedValueWhenCallingMethod("addOneByte", "-1", "0");

        assertEqualExpectedValueWhenCallingMethod("addOneByteBoxed", "1", "2");
        assertEqualExpectedValueWhenCallingMethod("addOneByteBoxed", "0", "1");
        assertEqualExpectedValueWhenCallingMethod("addOneByteBoxed", "-1", "0");
    }

    @Test
    public void should_ConvertNumberToByte_When_ReceiveNumberAsString() {
        assertEqualExpectedValueWhenCallingMethod("addOneByte", "\"1\"", "2");
        assertEqualExpectedValueWhenCallingMethod("addOneByte", "\"0\"", "1");
        assertEqualExpectedValueWhenCallingMethod("addOneByte", "\"-1\"", "0");

        assertEqualExpectedValueWhenCallingMethod("addOneByteBoxed", "\"1\"",
                "2");
        assertEqualExpectedValueWhenCallingMethod("addOneByteBoxed", "\"0\"",
                "1");
        assertEqualExpectedValueWhenCallingMethod("addOneByteBoxed", "\"-1\"",
                "0");
    }

    @Test
    public void should_ConvertNumberToByte_When_ReceiveDecimalAsNumber() {
        assertEqualExpectedValueWhenCallingMethod("addOneByte", "1.1", "2");
        assertEqualExpectedValueWhenCallingMethod("addOneByte", "0.0", "1");
        assertEqualExpectedValueWhenCallingMethod("addOneByte", "-1.9", "0");

        assertEqualExpectedValueWhenCallingMethod("addOneByteBoxed", "1.1",
                "2");
        assertEqualExpectedValueWhenCallingMethod("addOneByteBoxed", "0.0",
                "1");
        assertEqualExpectedValueWhenCallingMethod("addOneByteBoxed", "-1.9",
                "0");
    }

    @Test
    public void should_FailToConvertNumberToByte_When_ReceiveDecimalAsString() {
        assert400ResponseWhenCallingMethod("addOneByte", "\"1.1\"");

        assert400ResponseWhenCallingMethod("addOneByteBoxed", "\"1.1\"");
    }

    @Test
    public void should_HandleOverflowByte_When_ReceiveOverflowNumber() {
        String overflowInputByte = "128";
        String overflowInputByteString = "\"128\"";
        assertEqualExpectedValueWhenCallingMethod("addOneByte",
                overflowInputByte, String.valueOf(Byte.MIN_VALUE + 1));
        assertEqualExpectedValueWhenCallingMethod("addOneByteBoxed",
                overflowInputByte, String.valueOf(Byte.MIN_VALUE + 1));

        assertEqualExpectedValueWhenCallingMethod("addOneByte",
                overflowInputByteString, String.valueOf(Byte.MIN_VALUE + 1));
        assertEqualExpectedValueWhenCallingMethod("addOneByteBoxed",
                overflowInputByteString, String.valueOf(Byte.MIN_VALUE + 1));
    }

    @Test
    public void should_FailToHandleUnderflowByte_When_ReceiveUnderflowNumber() {
        String underflowInputByte = "-129";
        String underflowInputByteString = "\"-129\"";
        assert400ResponseWhenCallingMethod("addOneByte", underflowInputByte);
        assert400ResponseWhenCallingMethod("addOneByte",
                underflowInputByteString);

        assert400ResponseWhenCallingMethod("addOneByteBoxed",
                underflowInputByte);
        assert400ResponseWhenCallingMethod("addOneByteBoxed",
                underflowInputByteString);
    }

    @Test
    public void should_HandleSpecialInputForByte_When_SpecialInput() {
        assertEqualExpectedValueWhenCallingMethod("addOneByte", "null", "1");

        assertEqualExpectedValueWhenCallingMethod("addOneByteBoxed", "null",
                "null");
    }

    @Test
    public void should_Return400_When_InputNaN() {
        assert400ResponseWhenCallingMethod("addOneByte", "NaN");

        assert400ResponseWhenCallingMethod("addOneByteBoxed", "NaN");
    }
}
