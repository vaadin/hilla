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
package com.vaadin.hilla.parser.plugins.nonnull.nullable.nullmarked;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.vaadin.hilla.parser.plugins.nonnull.nullable.nullmarked.unmarked.UnmarkedModel;
import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

@Endpoint
public class NullMarkedEndpoint {

    public NullMarkedModel nullMarkedModel(NullMarkedModel nullMarkedModel) {
        return nullMarkedModel;
    }

    public @Nullable NullMarkedModel nullableMethod(@Nullable String param) {
        return null;
    }

    public List<@Nullable String> nullableTypeArgumentMethod(
            List<@Nullable String> param) {
        return param;
    }

    public @Nullable String[] nullableItemArray() {
        return new String[0];
    }

    public String @Nullable [] nullableArray() {
        return new String[0];
    }

    public UnmarkedModel unmarkedModel(UnmarkedModel unmarkedModel) {
        return unmarkedModel;
    }

    public static class NullMarkedModel {
        public String required;

        public @Nullable String nullable;

        private String nullableGetter;

        private List<String> nullableGetterItems;

        private String nullableSetter;

        private List<String> nullableSetterItems;

        private String ignoredOnGetter;

        private String ignoredOnSetter;

        private String ignoredOnSetterIncludedOnGetter;

        @JsonProperty
        private String ignoredOnGetterIncludedOnField;

        private String ignoredOnGetterIncludedOnSetter;

        private String notIgnoredOnGetter;

        public @Nullable String getNullableGetter() {
            return nullableGetter;
        }

        public List<@Nullable String> getNullableGetterItems() {
            return nullableGetterItems;
        }

        public String getNullableSetter() {
            return nullableSetter;
        }

        public void setNullableSetter(@Nullable String nullableSetter) {
            this.nullableSetter = nullableSetter;
        }

        public List<String> getNullableSetterItems() {
            return nullableSetterItems;
        }

        public void setNullableSetterItems(
                List<@Nullable String> nullableSetterItems) {
            this.nullableSetterItems = nullableSetterItems;
        }

        @JsonIgnore
        public String getIgnoredOnGetter() {
            return ignoredOnGetter;
        }

        public void setIgnoredOnGetter(String ignoredOnGetter) {
            this.ignoredOnGetter = ignoredOnGetter;
        }

        public String getIgnoredOnSetter() {
            return ignoredOnSetter;
        }

        @JsonIgnore
        public void setIgnoredOnSetter(String ignoredOnSetter) {
            this.ignoredOnSetter = ignoredOnSetter;
        }

        @JsonProperty
        public String getIgnoredOnSetterIncludedOnGetter() {
            return ignoredOnSetterIncludedOnGetter;
        }

        @JsonIgnore
        public void setIgnoredOnSetterIncludedOnGetter(
                String ignoredOnSetterIncludedOnGetter) {
            this.ignoredOnSetterIncludedOnGetter = ignoredOnSetterIncludedOnGetter;
        }

        @JsonIgnore
        public String getIgnoredOnGetterIncludedOnField() {
            return ignoredOnGetterIncludedOnField;
        }

        public void setIgnoredOnGetterIncludedOnField(
                String ignoredOnGetterIncludedOnField) {
            this.ignoredOnGetterIncludedOnField = ignoredOnGetterIncludedOnField;
        }

        @JsonIgnore
        public String getIgnoredOnGetterIncludedOnSetter() {
            return ignoredOnGetterIncludedOnSetter;
        }

        @JsonProperty
        public void setIgnoredOnGetterIncludedOnSetter(
                String ignoredOnGetterIncludedOnSetter) {
            this.ignoredOnGetterIncludedOnSetter = ignoredOnGetterIncludedOnSetter;
        }

        @JsonIgnore(false)
        public String getNotIgnoredOnGetter() {
            return notIgnoredOnGetter;
        }

        public void setNotIgnoredOnGetter(String notIgnoredOnGetter) {
            this.notIgnoredOnGetter = notIgnoredOnGetter;
        }
    }
}
