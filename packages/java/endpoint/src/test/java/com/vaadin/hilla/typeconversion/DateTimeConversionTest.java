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

public class DateTimeConversionTest extends BaseTypeConversionTest {

    // region date tests
    @Test
    public void should_ConvertToDate_When_ReceiveATimeStampAsNumber() {
        String timeStamp = "1546300800000"; // 01-01-2019 00:00:00
        String expectedValue = "\"2019-01-02T00:00:00.000+00:00\"";
        assertEqualExpectedValueWhenCallingMethod("addOneDayToDate", timeStamp,
                expectedValue);
    }

    @Test
    public void should_ConvertToDate_When_ReceiveATimeStampAsString() {
        String timeStamp = "\"1546300800000\""; // 01-01-2019 00:00:00
        String expected = "\"2019-01-02T00:00:00.000+00:00\"";
        assertEqualExpectedValueWhenCallingMethod("addOneDayToDate", timeStamp,
                expected);
    }

    @Test
    public void should_ConvertToDate_When_ReceiveADate() {
        String inputDate = "\"2019-01-01\"";
        String expectedTimestamp = "\"2019-01-02T00:00:00.000+00:00\"";
        assertEqualExpectedValueWhenCallingMethod("addOneDayToDate", inputDate,
                expectedTimestamp);
    }

    @Test
    public void should_ConvertToNullForDate_When_ReceiveANull() {
        String timeStamp = "null";
        assertEqualExpectedValueWhenCallingMethod("addOneDayToDate", timeStamp,
                timeStamp);
    }
    // endregion
    // region LocalDate tests
    // LocalDate uses java.time.format.DateTimeFormatter.ISO_LOCAL_DATE as
    // default
    // format

    @Test
    public void should_ConvertToLocalDate_When_ReceiveALocalDate() {
        String inputDate = "\"2019-12-13\"";
        String expectedTimestamp = "\"2019-12-14\"";
        assertEqualExpectedValueWhenCallingMethod("addOneDayLocalDate",
                inputDate, expectedTimestamp);
    }

    @Test
    public void should_FailToConvertToLocalDate_When_ReceiveWrongFormat() {
        String inputDate = "\"2019:12:13\"";
        assert400ResponseWhenCallingMethod("addOneDayLocalDate", inputDate);
    }
    // endregion

    // region LocalTime tests
    // LocalTime uses java.time.format.DateTimeFormatter.ISO_LOCAL_DATE as
    // default
    // format

    @Test
    public void should_ConvertToLocalTime_When_ReceiveALocalTime() {
        String inputTime = "\"12:12:12\"";
        String expectedTimestamp = "\"13:12:12\"";
        assertEqualExpectedValueWhenCallingMethod("addOneHourLocalTime",
                inputTime, expectedTimestamp);
    }

    @Test
    public void should_FailToConvertToLocalTime_When_ReceiveWrongFormat() {
        String inputTime = "\"12+12+12\"";
        assert400ResponseWhenCallingMethod("addOneHourLocalTime", inputTime);
    }
    // endregion

    // region LocalDateTime tests
    // LocalDate uses java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME as
    // default format
    @Test
    public void should_ConvertToLocalDateTime_When_ReceiveALocalDateTime() {
        String inputDate = "\"2019-12-13T12:12:12\"";
        String expectedTimestamp = "\"2019-12-14T13:12:12\"";
        assertEqualExpectedValueWhenCallingMethod(
                "addOneDayOneHourLocalDateTime", inputDate, expectedTimestamp);
    }

    @Test
    public void should_FailToConvertToLocalDateTime_When_ReceiveWrongFormat() {
        String inputDate = "\"2019-12-13\"";
        assert400ResponseWhenCallingMethod("addOneDayOneHourLocalDateTime",
                inputDate);
    }
    // endregion

}
