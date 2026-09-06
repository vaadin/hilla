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
package com.vaadin.hilla.parser.plugins.subtypes.typeids;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A hierarchy whose type ids come from the annotations, falling back to the
 * class name without its package.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ @JsonSubTypes.Type(value = NameBase.Named.class),
        @JsonSubTypes.Type(value = NameBase.Nested.class) })
public class NameBase {
    public String id;

    /**
     * A subtype with a name of its own.
     */
    @JsonTypeName("named")
    public static class Named extends NameBase {
        public String name;
    }

    /**
     * A subtype with no name anywhere: with {@code Id.NAME}, its id keeps the
     * enclosing class, unlike the simple class name.
     */
    public static class Nested extends NameBase {
        public String note;
    }
}
