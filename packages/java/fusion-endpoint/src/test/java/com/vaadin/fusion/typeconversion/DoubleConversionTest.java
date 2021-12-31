/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.fusion.typeconversion;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

public class DoubleConversionTest extends BaseTypeConversionTest {
    @Test
    public void should_ConvertToDouble_When_ReceiveANumber() {
        assertCallMethodWithExpectedDoubleValue("addOneDouble", "1", "2.0");
        assertCallMethodWithExpectedDoubleValue("addOneDouble", "-1", "0.0");
        assertCallMethodWithExpectedDoubleValue("addOneDouble", "0", "1.0");

        assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "1",
                "2.0");
        assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "-1",
                "0.0");
        assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "0",
                "1.0");
    }

    @Test
    public void should_ConvertToDouble_When_ReceiveANumberAsString() {
        assertCallMethodWithExpectedDoubleValue("addOneDouble", "\"1\"", "2.0");
        assertCallMethodWithExpectedDoubleValue("addOneDouble", "\"-1\"",
                "0.0");
        assertCallMethodWithExpectedDoubleValue("addOneDouble", "\"0\"", "1.0");

        assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "\"1\"",
                "2.0");
        assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "\"-1\"",
                "0.0");
        assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "\"0\"",
                "1.0");
    }

    @Test
    public void should_ConvertToDouble_When_ReceiveDecimalAsNumber() {
        assertCallMethodWithExpectedDoubleValue("addOneDouble", "1.1", "2.1");
        assertCallMethodWithExpectedDoubleValue("addOneDouble", "-1.9", "-0.9");

        assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "1.1",
                "2.1");
        assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "-1.9",
                "-0.9");
    }

    @Test
    public void should_ConvertToDouble_When_ReceiveDecimalAsNumberAsString() {
        assertCallMethodWithExpectedDoubleValue("addOneDouble", "\"1.1\"",
                "2.1");
        assertCallMethodWithExpectedDoubleValue("addOneDouble", "\"-1.9\"",
                "-0.9");

        assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "\"1.1\"",
                "2.1");
        assertCallMethodWithExpectedDoubleValue("addOneDoubleBoxed", "\"-1.9\"",
                "-0.9");
    }

    @Test
    public void should_HandleOverflowDouble_When_ReceiveANumberOverflowOrUnderflow() {
        String overflowDouble = "2.7976931348623158E308";
        String overflowDoubleString = "\"2.7976931348623158E308\"";
        String underflowDouble = "-2.7976931348623157E308";
        String underflowDoubleString = "\"-2.7976931348623157E308\"";
        assertEqualExpectedValueWhenCallingMethod("addOneDouble",
                overflowDouble, "\"" + Float.POSITIVE_INFINITY + "\"");
        assertEqualExpectedValueWhenCallingMethod("addOneDouble",
                overflowDoubleString, "\"" + Float.POSITIVE_INFINITY + "\"");

        assertEqualExpectedValueWhenCallingMethod("addOneDouble",
                underflowDouble, "\"" + Double.NEGATIVE_INFINITY + "\"");
        assertEqualExpectedValueWhenCallingMethod("addOneDouble",
                underflowDoubleString, "\"" + Double.NEGATIVE_INFINITY + "\"");

        assertEqualExpectedValueWhenCallingMethod("addOneDoubleBoxed",
                overflowDouble, "\"" + Float.POSITIVE_INFINITY + "\"");
        assertEqualExpectedValueWhenCallingMethod("addOneDoubleBoxed",
                overflowDoubleString, "\"" + Float.POSITIVE_INFINITY + "\"");

        assertEqualExpectedValueWhenCallingMethod("addOneDoubleBoxed",
                underflowDouble, "\"" + Double.NEGATIVE_INFINITY + "\"");
        assertEqualExpectedValueWhenCallingMethod("addOneDoubleBoxed",
                underflowDoubleString, "\"" + Double.NEGATIVE_INFINITY + "\"");
    }

    @Test
    public void should_ShouldHandleSpecialInputForDouble_When_ReceiveNull() {
        assertEqualExpectedValueWhenCallingMethod("addOneDouble", "null",
                "1.0");

        assertEqualExpectedValueWhenCallingMethod("addOneDoubleBoxed", "null",
                "null");
    }

    @Test
    public void should_Return400_When_ReceiveSpecialInput() {
        assert400ResponseWhenCallingMethod("addOneDouble", "NaN");
        assert400ResponseWhenCallingMethod("addOneDouble", "Infinity");
        assert400ResponseWhenCallingMethod("addOneDouble", "-Infinity");

        assert400ResponseWhenCallingMethod("addOneDoubleBoxed", "NaN");
        assert400ResponseWhenCallingMethod("addOneDoubleBoxed", "Infinity");
        assert400ResponseWhenCallingMethod("addOneDoubleBoxed", "-Infinity");
    }

    private void assertCallMethodWithExpectedDoubleValue(String methodName,
            String parameterValue, String expectedValue) {
        try {
            MockHttpServletResponse response = callMethod(methodName,
                    parameterValue);
            Assert.assertEquals(Double.valueOf(expectedValue),
                    Double.valueOf(response.getContentAsString()), 0.1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
