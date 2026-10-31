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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class SignaturesTests {
    private static AnnotatedType typeOf(String field) {
        try {
            return Sample.class.getDeclaredField(field).getAnnotatedType();
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static List<Class<?>> classesOf(List<AnnotatedType> types) {
        return types.stream()
                .<Class<?>> map(type -> Signatures.getClass(type).orElseThrow())
                .toList();
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({ "primitive, int, true, false, false",
            "wrapper, java.lang.Integer, false, true, false",
            "string, java.lang.String, false, true, false",
            "list, java.util.List, false, true, false",
            "array, , false, false, true",
            "typeVariable, , false, false, false" })
    public void should_TellTheKindOfASignature(String field, String className,
            boolean base, boolean classRef, boolean array) {
        var type = typeOf(field);

        assertEquals(Optional.ofNullable(className),
                Signatures.getClass(type).map(Class::getName));
        assertEquals(base, Signatures.isBase(type));
        assertEquals(classRef, Signatures.isClassRef(type));
        assertEquals(array, Signatures.isArray(type));
        assertEquals(field.equals("typeVariable"),
                Signatures.isTypeVariable(type));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({ "primitive, true, true, false, true",
            "wrapper, true, false, false, true",
            "string, false, false, true, true",
            "array, false, false, false, false",
            "typeVariable, false, false, false, false" })
    public void should_TellWhatASignatureStandsFor(String field,
            boolean integer, boolean primitive, boolean string,
            boolean jdkClass) {
        var type = typeOf(field);

        assertEquals(integer, Signatures.isInteger(type));
        assertEquals(primitive, Signatures.isPrimitive(type));
        assertEquals(string, Signatures.isString(type));
        assertEquals(jdkClass, Signatures.isJDKClass(type));
    }

    @Test
    public void should_TellATypeParameterFromATypeVariable() {
        var typeVariable = typeOf("typeVariable");
        var typeParameter = Sample.class.getTypeParameters()[0];

        assertTrue(Signatures.isTypeParameter(typeParameter));
        assertFalse(Signatures.isTypeVariable(typeParameter));
        assertFalse(Signatures.isTypeParameter(typeVariable));
        assertEquals(typeParameter,
                Signatures.resolve((AnnotatedTypeVariable) typeVariable));
        assertEquals(List.of(Number.class),
                classesOf(Signatures.getBounds(typeParameter)));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({ "anything, ''", "extending, java.lang.Number",
            "superOf, java.lang.Integer" })
    public void should_LeaveObjectOutOfTheBoundsOfAWildcard(String field,
            String bounds) {
        var wildcard = (AnnotatedWildcardType) Signatures
                .getTypeArguments(typeOf(field)).get(0);

        assertEquals(bounds.isEmpty() ? List.of() : List.of(bounds),
                Signatures.getBounds(wildcard).stream()
                        .map(type -> type.getType().getTypeName()).toList());
    }

    @Test
    public void should_KeepTypeArgumentsAndAnnotations_When_ReplacingTheClass() {
        var original = typeOf("annotatedCollection");

        var replaced = Signatures.withClass(original, Map.class);

        assertEquals(Optional.of(Map.class), Signatures.getClass(replaced));
        assertTrue(replaced instanceof AnnotatedParameterizedType);
        assertEquals(Signatures.getTypeArguments(original),
                Signatures.getTypeArguments(replaced));
        assertEquals(Signatures.getAnnotations(original),
                Signatures.getAnnotations(replaced));
        assertTrue(replaced.isAnnotationPresent(Marker.class));
    }

    @Test
    public void should_KeepAnnotations_When_ReplacingAClassWithoutTypeArguments() {
        var original = typeOf("annotatedString");

        var replaced = Signatures.withClass(original, Object.class);

        assertEquals(Optional.of(Object.class), Signatures.getClass(replaced));
        assertTrue(Signatures.getTypeArguments(replaced).isEmpty());
        assertEquals(Signatures.getAnnotations(original),
                Signatures.getAnnotations(replaced));
    }

    @Test
    public void should_CreateASignatureWithoutAnnotations_ForABareClass() {
        var bare = Signatures.of(Marked.class);

        assertEquals(Optional.of(Marked.class), Signatures.getClass(bare));
        assertTrue(Signatures.isClassRef(bare));
        assertTrue(Signatures.getAnnotations(bare).isEmpty());
    }

    @Test
    public void should_CheckOnlySuperclasses_When_CheckingAssignabilityByName() {
        assertTrue(Signatures.isAssignableFrom("java.util.AbstractList",
                ArrayList.class));
        assertFalse(Signatures.isAssignableFrom(Collection.class.getName(),
                ArrayList.class));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE_USE, ElementType.TYPE })
    @interface Marker {
    }

    @Marker
    static class Marked {
    }

    static class Sample<T extends Number> {
        int primitive;
        Integer wrapper;
        String string;
        List<String> list;
        String[] array;
        T typeVariable;
        List<?> anything;
        List<? extends Number> extending;
        List<? super Integer> superOf;
        @Marker
        Collection<String> annotatedCollection;
        @Marker
        String annotatedString;
    }
}
