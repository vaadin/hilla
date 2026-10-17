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
package com.vaadin.hilla.parser.plugins.backbone;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * What the walk says about an entity beyond the class it is written from and
 * the properties the walk carries below it.
 */
public final class EntityFacts {
    private Discriminator discriminator;
    private Map<String, String> enumValues = Map.of();

    /**
     * The property saying which subtype of a polymorphic type a value is, which
     * an entity has where it belongs to such a hierarchy.
     */
    public Optional<Discriminator> getDiscriminator() {
        return Optional.ofNullable(discriminator);
    }

    public void setDiscriminator(Discriminator discriminator) {
        this.discriminator = discriminator;
    }

    /**
     * The value each constant of an enum is serialized as, keyed by the name of
     * the constant, in the order the enum declares them. Empty for a type which
     * is not an enum.
     */
    public Map<String, String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(Map<String, String> enumValues) {
        this.enumValues = Collections
                .unmodifiableMap(new LinkedHashMap<>(enumValues));
    }

    /**
     * The property saying which subtype a value is.
     *
     * @param acceptedValues
     *            the values the property can hold, which are the id of the type
     *            itself followed by the ids of the subtypes below it
     */
    public record Discriminator(String name, List<String> acceptedValues) {
        public Discriminator {
            Objects.requireNonNull(name);
            acceptedValues = List.copyOf(acceptedValues);
        }
    }
}
