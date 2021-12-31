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
package com.vaadin.fusion.generator.endpoints.datetime;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.vaadin.fusion.Endpoint;

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

    public List<LocalDateTime> echoListLocalDateTime(
            List<LocalDateTime> localDateTimeList) {
        return localDateTimeList;
    }

    public Map<String, Instant> echoMapInstant(
            Map<String, Instant> mapInstant) {
        return mapInstant;
    }
}
