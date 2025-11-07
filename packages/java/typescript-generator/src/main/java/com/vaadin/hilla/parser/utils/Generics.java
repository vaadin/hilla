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

import com.googlecode.gentyref.GenericTypeReflector;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * Utility class for working with generics.
 */
public class Generics {
    /**
     * Returns the exact type of the elements in an iterable class.
     *
     * @param cls
     *            the class to get the exact type of
     * @return the exact type of the elements in the iterable class
     */
    public static Optional<Type> getExactIterableType(Class<?> cls) {
        try {
            // We can find the exact type by looking at the iterator method
            // return type
            var method = cls.getMethod("iterator");
            var exactReturnType = GenericTypeReflector
                    .getExactReturnType(method, cls);

            // We know the format of the method, so we can safely cast the type
            if (exactReturnType instanceof ParameterizedType) {
                return Optional.of(((ParameterizedType) exactReturnType)
                        .getActualTypeArguments()[0]);
            }

            return Optional.empty();
        } catch (NoSuchMethodException e) {
            // This should really never happen if the passed class is an
            // Iterable
            throw new IllegalArgumentException(
                    "Class " + cls.getName() + " is not an Iterable");
        }
    }
}
