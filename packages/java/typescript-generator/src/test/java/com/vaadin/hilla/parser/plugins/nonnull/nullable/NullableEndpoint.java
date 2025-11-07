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
package com.vaadin.hilla.parser.plugins.nonnull.nullable;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Endpoint
public class NullableEndpoint {

    public NullableFieldModel nullableFieldModel(
            NullableFieldModel nullableFieldModel) {
        return nullableFieldModel;
    }

    public static class NullableFieldModel {
        @Id
        public String id;
        @Version
        public Long version;

        @jakarta.annotation.Nonnull
        public String jakartaNonnull;

        @org.jspecify.annotations.NonNull
        public String jspecifyNonnull;

        @org.springframework.lang.NonNull
        public String springNonnull;
    }
}
