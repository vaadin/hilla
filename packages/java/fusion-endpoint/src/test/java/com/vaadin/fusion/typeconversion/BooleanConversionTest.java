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
package com.vaadin.fusion.typeconversion;

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
