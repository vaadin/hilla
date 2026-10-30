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
package com.vaadin.hilla.parser.utils;

import java.lang.reflect.Type;
import java.util.Optional;

import org.springframework.core.ResolvableType;

/**
 * Utility class for working with generics.
 */
public class Generics {
    /**
     * Returns the exact type of the elements in an iterable class.
     *
     * @param cls
     *            the class to get the exact type of
     * @return the class of the elements in the iterable class, or an empty
     *         optional if it cannot be resolved, e.g. because the class is used
     *         as a raw type
     */
    public static Optional<Type> getExactIterableType(Class<?> cls) {
        if (!Iterable.class.isAssignableFrom(cls)) {
            throw new IllegalArgumentException(
                    "Class " + cls.getName() + " is not an Iterable");
        }
        return Optional.ofNullable(ResolvableType.forClass(cls)
                .as(Iterable.class).getGeneric(0).resolve());
    }
}
