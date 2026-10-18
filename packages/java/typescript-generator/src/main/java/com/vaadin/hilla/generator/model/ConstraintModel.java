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
package com.vaadin.hilla.generator.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * What a value has to satisfy, as one of the annotations of the validation API
 * says it, which a form model binds as a validator of the property holding the
 * value.
 *
 * @param name
 *            the simple name of the annotation, which is the name the form
 *            library knows the validator by
 * @param attributes
 *            what the annotation says, by the name of the attribute, with the
 *            ones it says nothing about left out; the order is the one of the
 *            names, so that the same class is always written the same way
 */
public record ConstraintModel(String name, Map<String, Object> attributes) {
    public ConstraintModel {
        Objects.requireNonNull(name);
        attributes = Collections.unmodifiableMap(
                new TreeMap<>(Objects.requireNonNull(attributes)));
    }

    public static ConstraintModel of(String name) {
        return new ConstraintModel(name, Map.of());
    }
}
