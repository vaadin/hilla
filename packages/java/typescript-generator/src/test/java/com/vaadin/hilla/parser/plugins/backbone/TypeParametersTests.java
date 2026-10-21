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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.function.Supplier;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.vaadin.hilla.parser.models.TypeParameterModel;

public class TypeParametersTests {
    @ParameterizedTest(name = "{0}.{1} unbounded: {2}")
    @CsvSource({ "Plain, T, true", "Bounded, T, false", "FBounded, E, true",
            "ArrayFBounded, T, true", "ArrayBounded, T, false",
            "BoundedByFBounded, A, false", "BoundedByFBounded, B, true" })
    public void should_TellWhetherTypeParameterIsUnbounded(String sample,
            String typeParameterName, boolean expected)
            throws ClassNotFoundException {
        var typeParameter = Arrays
                .stream(Class.forName(
                        TypeParametersTests.class.getName() + '$' + sample)
                        .getTypeParameters())
                .filter(origin -> origin.getName().equals(typeParameterName))
                .findFirst().map(TypeParameterModel::of).orElseThrow();

        assertEquals(expected, TypeParameters.isUnbounded(typeParameter));
    }

    static final class Plain<T> {
    }

    static final class Bounded<T extends Number> {
    }

    static final class FBounded<E extends Enum<E>> {
    }

    static final class ArrayFBounded<T extends Supplier<T[]>> {
    }

    static final class ArrayBounded<T extends Supplier<int[]>> {
    }

    /**
     * {@code A} is bounded by {@code B}, which is F-bounded on its own, so
     * following the bound of {@code A} meets {@code B} again without ever
     * leading back to {@code A}.
     */
    static final class BoundedByFBounded<A extends Comparable<B>, B extends Comparable<B>> {
    }
}
