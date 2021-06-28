package com.vaadin.fusion;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

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
        assertTrue(field.getAnnotatedType().isAnnotationPresent(Nonnull.class));
    }

    @Test
    public void should_haveFieldWithNonNullableCollectionItem() {
        AnnotatedType listItemType = ((AnnotatedParameterizedType) field
                .getAnnotatedType()).getAnnotatedActualTypeArguments()[0];
        assertTrue(listItemType.isAnnotationPresent(Nonnull.class));
    }

    @Test
    public void should_haveMethodWithNonNullableReturnType() {
        assertTrue(method.getAnnotatedReturnType()
                .isAnnotationPresent(Nonnull.class));
    }

    @Test
    public void should_haveMethodWithNonNullableParameter() {
        assertTrue(parameter.getAnnotatedType()
                .isAnnotationPresent(Nonnull.class));
    }

    @Test
    public void should_haveMethodParameterWithNonNullableCollectionItemType() {
        AnnotatedType mapValueType = ((AnnotatedParameterizedType) parameter
                .getAnnotatedType()).getAnnotatedActualTypeArguments()[1];

        assertTrue(mapValueType.isAnnotationPresent(Nonnull.class));
    }
}
