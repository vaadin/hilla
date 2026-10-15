/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla.rest;

import java.time.LocalTime;
import java.time.ZonedDateTime;

import com.vaadin.hilla.Endpoint;

@Endpoint
public class TestEndpoints {

    byte[] arr = { 1, 2, 3, 4 };

    public BeanWithZonedDateTimeField getBeanWithZonedDateTimeField() {
        return new BeanWithZonedDateTimeField();
    }

    public BeanWithPrivateFields getBeanWithPrivateFields() {
        return new BeanWithPrivateFields();
    }

    public BeanWithJacksonAnnotation getBeanWithJacksonAnnotation() {
        return new BeanWithJacksonAnnotation();
    }

    public byte[] getByteArray() {
        return arr;
    }

    public LocalTime getLocalTime() {
        return LocalTime.of(8, 0, 0);
    }

    public static class BeanWithZonedDateTimeField {
        private ZonedDateTime zonedDateTime = ZonedDateTime.now();

        public ZonedDateTime getZonedDateTime() {
            return zonedDateTime;
        }

        public void setZonedDateTime(ZonedDateTime zonedDateTime) {
            this.zonedDateTime = zonedDateTime;
        }
    }
}
