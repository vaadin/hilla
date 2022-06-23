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
package dev.hilla.typeconversion;

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
