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

public class LongConversionTest extends BaseTypeConversionTest {

    @Test
    public void should_ConvertToLong_When_ReceiveANumber() {
        assertEqualExpectedValueWhenCallingMethod("addOneLong", "1", "2");
        assertEqualExpectedValueWhenCallingMethod("addOneLong", "-1", "0");
        assertEqualExpectedValueWhenCallingMethod("addOneLong", "0", "1");

        assertEqualExpectedValueWhenCallingMethod("addOneLongBoxed", "1", "2");
        assertEqualExpectedValueWhenCallingMethod("addOneLongBoxed", "-1", "0");
        assertEqualExpectedValueWhenCallingMethod("addOneLongBoxed", "0", "1");
    }

    @Test
    public void should_ConvertToLong_When_ReceiveANumberAsString() {

        assertEqualExpectedValueWhenCallingMethod("addOneLong", "\"1\"", "2");
        assertEqualExpectedValueWhenCallingMethod("addOneLong", "\"-1\"", "0");
        assertEqualExpectedValueWhenCallingMethod("addOneLong", "\"0\"", "1");

        assertEqualExpectedValueWhenCallingMethod("addOneLongBoxed", "\"1\"",
                "2");
        assertEqualExpectedValueWhenCallingMethod("addOneLongBoxed", "\"-1\"",
                "0");
        assertEqualExpectedValueWhenCallingMethod("addOneLongBoxed", "\"0\"",
                "1");
    }

    @Test
    public void should_FailToConvertToLong_When_ReceiveOverflowLongAsString() {
        String overflowLong = "\"9223372036854775808\"";
        assert400ResponseWhenCallingMethod("addOneLong", overflowLong);
        assert400ResponseWhenCallingMethod("addOneLongBoxed", overflowLong);

        String underflowLong = "\"-9223372036854775809\"";
        assert400ResponseWhenCallingMethod("addOneLong", underflowLong);
        assert400ResponseWhenCallingMethod("addOneLongBoxed", underflowLong);
    }

    @Test
    public void should_ConvertToLong_When_ReceiveDecimalAsNumber() {
        assertEqualExpectedValueWhenCallingMethod("addOneLong", "1.9", "2");
        assertEqualExpectedValueWhenCallingMethod("addOneLong", "-1.0", "0");
        assertEqualExpectedValueWhenCallingMethod("addOneLong", "0.0", "1");

        assertEqualExpectedValueWhenCallingMethod("addOneLongBoxed", "1.9",
                "2");
        assertEqualExpectedValueWhenCallingMethod("addOneLongBoxed", "-1.0",
                "0");
        assertEqualExpectedValueWhenCallingMethod("addOneLongBoxed", "0.0",
                "1");
    }

    @Test
    public void should_FailToConvertToLong_When_ReceiveDecimalAsString() {
        assert400ResponseWhenCallingMethod("addOneLong", "\"1.1\"");

        assert400ResponseWhenCallingMethod("addOneLongBoxed", "\"1.1\"");
    }

    @Test
    public void should_FailToConvertToLong_When_ReceiveANumberOverflowOrUnderflow() {
        String overflowLong = "9223372036854775808"; // 2^63
        assert400ResponseWhenCallingMethod("addOneLong", overflowLong);
        assert400ResponseWhenCallingMethod("addOneLongBoxed", overflowLong);

        String underflowLong = "-9223372036854775809"; // -2^63-1
        assert400ResponseWhenCallingMethod("addOneLong", underflowLong);
        assert400ResponseWhenCallingMethod("addOneLongBoxed", underflowLong);
    }

    @Test
    public void should_HandleSpecialInputForLong_When_ReceiveNull() {
        assertEqualExpectedValueWhenCallingMethod("addOneLong", "null", "1");
        assertEqualExpectedValueWhenCallingMethod("addOneLongBoxed", "null",
                "null");
    }

    @Test
    public void should_HandleSpecialInputForLong_When_ReceiveASpecialInput() {
        assert400ResponseWhenCallingMethod("addOneLong", "NaN");
        assert400ResponseWhenCallingMethod("addOneLongBoxed", "NaN");
    }
}
