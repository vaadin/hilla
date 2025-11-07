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
package com.vaadin.hilla.parser.plugins.nonnull.nullable.nonNullApi;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

import com.vaadin.hilla.parser.plugins.nonnull.nullable.Endpoint;
import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

@Endpoint
public class NullableNonNullEndpoint {

    public NullableNonNullFieldModel nullableNonNullFieldModel(
            NullableNonNullFieldModel nullableNonNullFieldModel) {
        return nullableNonNullFieldModel;
    }

    public static class NullableNonNullFieldModel {
        public String required;
        @Id
        public String id;
        @Version
        public Long version;
        @Version
        @Nonnull
        public Long notNullVersion;

        // it's easier to test nullability inside @NonNullApi context
        @jakarta.annotation.Nullable
        public String jakartaNullable;

        @org.jspecify.annotations.Nullable
        public String jspecifyNullable;

        @org.springframework.lang.Nullable
        public String springNullable;
    }
}
