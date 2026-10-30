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

import java.util.Objects;

/**
 * A property of an entity, under the name it is serialized as, which is not
 * necessarily the name of the Java field or of the method reading it.
 */
public record PropertyModel(String name, TypeModel type) {
    public PropertyModel {
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
    }
}
