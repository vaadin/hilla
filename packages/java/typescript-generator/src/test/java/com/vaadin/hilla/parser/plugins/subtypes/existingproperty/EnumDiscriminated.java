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
 * A hierarchy whose discriminator is an enum declared by a subtype rather than
 * by the base. Nothing the subtype extends declares the property, so it is
 * pinned to the type ids it accepts, {@code 'BITTER' | 'VERY_BITTER'}, which
 * contradicts no declaration; the subtype below it narrows that to its own id.
 *
 * <p>
 * The {@code Taste} enum is still generated, but a TypeScript string enum is
 * not assignable to the literals, so a payload for these types is written with
 * the literal rather than the enum constant. That is the price of keeping a
 * discriminant the union can be told apart by.
 *
 * <p>
 * Between the two subtypes sits a class that is not a subtype and inherits the
 * property rather than declaring it, so it neither has a discriminator of its
 * own nor counts as declaring the enum above the subtype below it.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "taste")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EnumDiscriminated.Bitter.class, name = "BITTER"),
        @JsonSubTypes.Type(value = EnumDiscriminated.VeryBitter.class, name = "VERY_BITTER") })
public class EnumDiscriminated {
    public String label;

    public enum Taste {
        BITTER, VERY_BITTER
    }

    public static class Bitter extends EnumDiscriminated {
        public String note;
        public Taste taste;
    }

    public static class Roasted extends Bitter {
        public String origin;
    }

    public static class VeryBitter extends Roasted {
        public int level;
    }
}
