/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla.typeconversion;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class EndpointTestBean {
    public String name;
    public String address;
    public int age;
    public boolean isAdmin;
    public TestTypeConversionEndpoints.TestEnum testEnum;
    public Collection<String> roles;
    private String customProperty;

    @JsonGetter("customProperty")
    public String getCustomProperty() {
        return customProperty;
    }

    @JsonSetter("customProperty")
    public void setCustomProperty(String customProperty) {
        this.customProperty = customProperty;
    }
}
