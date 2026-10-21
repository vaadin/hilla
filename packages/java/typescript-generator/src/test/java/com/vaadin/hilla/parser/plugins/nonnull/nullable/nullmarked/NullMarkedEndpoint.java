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
    }
}
