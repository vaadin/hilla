/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla.typeconversion;

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
