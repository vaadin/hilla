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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A hierarchy whose discriminator is an enum declared by the subtypes alone.
 * Nothing above them declares it, so pinning it to their type ids contradicts
 * no declaration: each subtype gets its own id, and the union can be told apart
 * by it.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "taste")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EnumDiscriminated.Bitter.class, name = "BITTER"),
        @JsonSubTypes.Type(value = EnumDiscriminated.Salty.class, name = "SALTY") })
public class EnumDiscriminated {
    public String label;

    public enum Taste {
        BITTER, SALTY
    }

    public static class Bitter extends EnumDiscriminated {
        /**
         * Read from a getter rather than from a field of its own, which is how
         * Jackson usually reads a property, and serialized under a name other
         * than its Java one: the discriminator is the name Jackson writes.
         */
        @JsonProperty("taste")
        public Taste getFlavour() {
            return Taste.BITTER;
        }

        public String note;
    }

    public static class Salty extends EnumDiscriminated {
        public Taste taste;
        public int level;
    }
}
