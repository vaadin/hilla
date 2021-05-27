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
