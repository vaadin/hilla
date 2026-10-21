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
package com.vaadin.hilla.nonnullapi;

import jakarta.persistence.Id;

import org.springframework.lang.Nullable;

/**
 * A bean with nullability annotated on the getter or setter instead of the
 * field, in a package annotated with Spring's {@code @NonNullApi}.
 */
public class NonNullApiProperties {
    @Id
    private String id = "";
    private String value = "";
    private String nullableGetter = "";
    private String nullableSetter = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Nullable
    public String getNullableGetter() {
        return nullableGetter;
    }

    public void setNullableGetter(String nullableGetter) {
        this.nullableGetter = nullableGetter;
    }

    public String getNullableSetter() {
        return nullableSetter;
    }

    public void setNullableSetter(@Nullable String nullableSetter) {
        this.nullableSetter = nullableSetter;
    }
}
