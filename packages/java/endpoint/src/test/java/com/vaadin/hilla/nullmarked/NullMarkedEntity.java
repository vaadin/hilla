/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.hilla.nullmarked;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

public class NullMarkedEntity {
    private String value;
    private @Nullable String nullableValue;
    private List<String> items = new ArrayList<>();
    private List<@Nullable String> nullableItems = new ArrayList<>();

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public @Nullable String getNullableValue() {
        return nullableValue;
    }

    public void setNullableValue(@Nullable String nullableValue) {
        this.nullableValue = nullableValue;
    }

    public List<String> getItems() {
        return items;
    }

    public List<@Nullable String> getNullableItems() {
        return nullableItems;
    }
}
