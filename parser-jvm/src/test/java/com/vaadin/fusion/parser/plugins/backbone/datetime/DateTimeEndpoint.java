package com.vaadin.fusion.parser.plugins.backbone.datetime;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Endpoint
public class DateTimeEndpoint {
    public Instant echoInstant(Instant instant) {
        return instant;
    }

    public Date echoDate(Date date) {
        return date;
    }

    public LocalDate echoLocalDate(LocalDate localDate) {
        return localDate;
    }

    public LocalDateTime echoLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime;
    }

    public LocalTime echoLocalTime(LocalTime localTime) {
        return localTime;
    }

    public CustomDate echoCustomDate() {
        return new CustomDate();
    }

    public List<LocalDateTime> echoListLocalDateTime(
            List<LocalDateTime> localDateTimeList) {
        return localDateTimeList;
    }

    public Map<String, Instant> echoMapInstant(
            Map<String, Instant> mapInstant) {
        return mapInstant;
    }

    public static class CustomDate extends Date {
    }
}
