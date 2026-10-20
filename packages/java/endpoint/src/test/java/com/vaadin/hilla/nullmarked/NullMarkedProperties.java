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

import jakarta.persistence.Id;
import jakarta.persistence.Version;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A bean with nullability annotated on the getter or setter instead of the
 * field.
 */
public class NullMarkedProperties {
    @Id
    private String id = "";
    @Version
    private Long version = 0L;
    @Id
    private String nonnullId = "";
    private String ignoredOnGetter = "";
    private String ignoredOnSetter = "";
    private String nullableGetter = "";
    private String nullableSetter = "";
    private String nullableGetterNonnullSetter = "";
    private List<String> nullableGetterItems = new ArrayList<>();
    private List<String> nullableSetterItems = new ArrayList<>();
    private ArrayList<@Nullable String> nullableFieldOfOtherTypeItems = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public @NonNull String getNonnullId() {
        return nonnullId;
    }

    public void setNonnullId(String nonnullId) {
        this.nonnullId = nonnullId;
    }

    public String getIgnoredOnSetter() {
        return ignoredOnSetter;
    }

    @JsonIgnore
    public void setIgnoredOnSetter(String ignoredOnSetter) {
        this.ignoredOnSetter = ignoredOnSetter;
    }

    @JsonIgnore
    public String getIgnoredOnGetter() {
        return ignoredOnGetter;
    }

    public void setIgnoredOnGetter(String ignoredOnGetter) {
        this.ignoredOnGetter = ignoredOnGetter;
    }

    public @Nullable String getNullableGetter() {
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

    public @Nullable String getNullableGetterNonnullSetter() {
        return nullableGetterNonnullSetter;
    }

    public void setNullableGetterNonnullSetter(
            @NonNull String nullableGetterNonnullSetter) {
        this.nullableGetterNonnullSetter = nullableGetterNonnullSetter;
    }

    public List<@Nullable String> getNullableGetterItems() {
        return nullableGetterItems;
    }

    public List<String> getNullableSetterItems() {
        return nullableSetterItems;
    }

    public void setNullableSetterItems(
            List<@Nullable String> nullableSetterItems) {
        this.nullableSetterItems = nullableSetterItems;
    }

    public List<String> getNullableFieldOfOtherTypeItems() {
        return nullableFieldOfOtherTypeItems;
    }
}
