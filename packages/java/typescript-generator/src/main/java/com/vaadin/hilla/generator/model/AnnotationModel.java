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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An annotation of a value which a form model is told about, such as the one
 * saying that a value is the id of an entity.
 *
 * @param name
 *            the fully qualified name of the annotation
 * @param attributes
 *            what the annotation says, by the name of the attribute, with the
 *            ones it says nothing about left out
 */
public record AnnotationModel(String name, Map<String, Object> attributes) {
    public AnnotationModel {
        Objects.requireNonNull(name);
        attributes = Collections.unmodifiableMap(
                new LinkedHashMap<>(Objects.requireNonNull(attributes)));
    }

    public static AnnotationModel of(String name) {
        return new AnnotationModel(name, Map.of());
    }
}
