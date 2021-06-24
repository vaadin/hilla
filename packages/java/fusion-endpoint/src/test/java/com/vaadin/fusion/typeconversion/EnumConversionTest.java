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
