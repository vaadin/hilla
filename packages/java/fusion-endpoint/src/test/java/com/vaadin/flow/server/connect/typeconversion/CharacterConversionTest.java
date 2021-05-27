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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

public class CharacterConversionTest extends BaseTypeConversionTest {
    @Test
    public void should_ConvertToChar_When_ReceiveASingleCharOrNumber()
            throws Exception {
        assertEqualExpectedValueWhenCallingMethod("getChar", "\"a\"", "\"a\"");
        assertEqualExpectedValueWhenCallingMethod("getCharBoxed", "\"a\"",
                "\"a\"");

        int maxValueCanBeCastToChar = 0xFFFF;
        MockHttpServletResponse response = callMethod("getChar",
                String.valueOf(maxValueCanBeCastToChar));
        Assert.assertEquals((char) maxValueCanBeCastToChar,
                getCharFromResponse(response.getContentAsString()));
        response = callMethod("getCharBoxed",
                String.valueOf(maxValueCanBeCastToChar));
        Assert.assertEquals((char) maxValueCanBeCastToChar,
                getCharFromResponse(response.getContentAsString()));

    }

    @Test
    public void should_FailToConvertToChar_When_ReceiveOverflowUnderflowNumber() {
        int overflowCharNumber = 0xFFFF + 1;
        int underflowCharNumber = -1;
        assert400ResponseWhenCallingMethod("getChar",
                String.valueOf(overflowCharNumber));
        assert400ResponseWhenCallingMethod("getChar",
                String.valueOf(underflowCharNumber));

        assert400ResponseWhenCallingMethod("getCharBoxed",
                String.valueOf(overflowCharNumber));
        assert400ResponseWhenCallingMethod("getCharBoxed",
                String.valueOf(underflowCharNumber));
    }

    @Test
    public void should_FailToConvertToChar_When_ReceiveInvalidNumber() {
        assert400ResponseWhenCallingMethod("getChar", "1.1");
        assert400ResponseWhenCallingMethod("getCharBoxed", "1.1");

        assert400ResponseWhenCallingMethod("getChar", "-1");
        assert400ResponseWhenCallingMethod("getCharBoxed", "-1");

        int overMax = 0xFFFF + 1;
        assert400ResponseWhenCallingMethod("getChar", String.valueOf(overMax));
        assert400ResponseWhenCallingMethod("getCharBoxed",
                String.valueOf(overMax));
    }

    @Test
    public void should_FailToConvertToChar_When_ReceiveLongString() {
        assert400ResponseWhenCallingMethod("getChar", "\"aa\"");

        assert400ResponseWhenCallingMethod("getCharBoxed", "\"aa\"");
    }

    private char getCharFromResponse(String response) {
        if (response.length() > 3) {
            return (char) Integer
                    .parseInt(response.substring(3, response.length() - 1));
        } else {
            return response.charAt(1);
        }
    }
}
