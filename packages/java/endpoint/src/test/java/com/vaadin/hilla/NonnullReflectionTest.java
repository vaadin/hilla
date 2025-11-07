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
package com.vaadin.hilla;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.junit.Before;
import org.junit.Test;

public class NonnullReflectionTest {
    private Field field;
    private Method method;
    private Parameter parameter;

    @Before
    public void init() throws NoSuchFieldException, NoSuchMethodException {
        field = NonnullEntity.class.getDeclaredField("nonNullableField");
        method = NonnullEntity.class.getDeclaredMethod("nonNullableMethod",
                Map.class);
        parameter = method.getParameters()[0];
    }

    @Test
    public void should_haveNonNullableField() {
        assertTrue(field.getAnnotatedType().isAnnotationPresent(NonNull.class));
    }

    @Test
    public void should_haveFieldWithNonNullableCollectionItem() {
        AnnotatedType listItemType = ((AnnotatedParameterizedType) field
                .getAnnotatedType()).getAnnotatedActualTypeArguments()[0];
        assertTrue(listItemType.isAnnotationPresent(NonNull.class));
    }

    @Test
    public void should_haveMethodWithNonNullableReturnType() {
        assertTrue(method.getAnnotatedReturnType()
                .isAnnotationPresent(NonNull.class));
    }

    @Test
    public void should_haveMethodWithNonNullableParameter() {
        assertTrue(parameter.getAnnotatedType()
                .isAnnotationPresent(NonNull.class));
    }

    @Test
    public void should_haveMethodParameterWithNonNullableCollectionItemType() {
        AnnotatedType mapValueType = ((AnnotatedParameterizedType) parameter
                .getAnnotatedType()).getAnnotatedActualTypeArguments()[1];

        assertTrue(mapValueType.isAnnotationPresent(NonNull.class));
    }
}
