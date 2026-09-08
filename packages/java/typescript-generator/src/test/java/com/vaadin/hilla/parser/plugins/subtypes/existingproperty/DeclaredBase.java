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
package com.vaadin.hilla.parser.plugins.subtypes.existingproperty;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A hierarchy whose discriminator is a property the types declare themselves,
 * which Jackson both reads and writes as an ordinary property.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DeclaredBase.First.class, name = "first"),
        @JsonSubTypes.Type(value = DeclaredBase.Second.class, name = "second") })
public class DeclaredBase {
    public String label;

    public static class First extends DeclaredBase {
        /**
         * The subtype declares the discriminator itself, which is what
         * {@code As.EXISTING_PROPERTY} means: it is an ordinary property of the
         * type as well as the type id.
         */
        public String kind;

        public String note;
    }

    public static class Second extends DeclaredBase {
        public String kind;

        public int size;
    }
}
