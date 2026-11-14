/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import static com.vaadin.hilla.ExplicitNullableTypeChecker.isRequired;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNullApi;

import com.vaadin.hilla.parser.utils.Generics;

/**
 * A helper class for ExplicitNullableTypeChecker.
 */
class ExplicitNullableTypeCheckerHelper {

    // A map for tracking already visited Beans.
    private Map<Type, Set<Object>> visitedBeans;

    private static Logger getLogger() {
        return LoggerFactory.getLogger(EndpointController.class);
    }

    /**
     * Check if the Bean value and type have been visited.
     */
    boolean hasVisited(Object value, Type type) {
        if (visitedBeans == null) {
            return false;
        }
        Set<Object> values = visitedBeans.get(type);
        if (values == null) {
            return false;
        }
        return values.contains(value);
    }

    /**
     * Mark the given value and type have been visited.
     */
    void markAsVisited(Object value, Type type) {
        if (visitedBeans == null) {
            visitedBeans = new HashMap<>();
        }
        visitedBeans.putIfAbsent(type, new HashSet<>());
        visitedBeans.get(type).add(value);
    }

    /**
     * Validates the given value for the given expected method parameter or
     * return value type.
     *
     * @param value
     *            the value to validate
     * @param expectedType
     *            the declared type expected for the value
     * @return error message when the value is null while the expected type does
     *         not explicitly allow null, or null meaning the value is OK.
     */
    String checkValueForType(Object value, Type expectedType) {
        return checkValueForType(value, expectedType, null, true);
    }

    /**
     * Validates the given value for the given expected annotated type. The
     * value itself is required, while the nullability of nested values, such as
     * collection items, array elements and map values, follows their type-use
     * annotations and the given context, the same way as in the generated
     * TypeScript.
     *
     * @param value
     *            the value to validate
     * @param expectedType
     *            the declared annotated type expected for the value
     * @param requiredByContext
     *            {@code true} if the context defines that the nested values are
     *            required
     * @return error message when the value is null while the expected type does
     *         not explicitly allow null, or null meaning the value is OK.
     */
    String checkValueForAnnotatedType(Object value, AnnotatedType expectedType,
            boolean requiredByContext) {
        return checkValueForType(value, expectedType.getType(),
                List.of(expectedType), requiredByContext);
    }

    /**
     * Validates the given value for the given expected type, declared by the
     * given annotated types. When the annotated types are unknown, nested
     * values are always required.
     */
    String checkValueForType(Object value, Type expectedType,
            @Nullable List<AnnotatedType> annotatedTypes,
            boolean requiredByContext) {
        Class<?> clazz;
        if (expectedType instanceof TypeVariable) {
            return null;
        } else if (expectedType instanceof ParameterizedType) {
            clazz = (Class<?>) ((ParameterizedType) expectedType).getRawType();
        } else {
            clazz = (Class<?>) expectedType;
        }

        if (value != null) {
            if (Iterable.class.isAssignableFrom(clazz)) {
                return checkIterable((Iterable<?>) value, expectedType,
                        annotatedTypes, requiredByContext);
            } else if (clazz.isArray() && value instanceof Object[]) {
                return checkIterable(Arrays.asList((Object[]) value),
                        expectedType, annotatedTypes, requiredByContext);
            } else if (Map.class.isAssignableFrom(clazz)) {
                return checkMapValues((Map<?, ?>) value, expectedType,
                        annotatedTypes, requiredByContext);
            } else if (expectedType instanceof Class<?>
                    && !clazz.getName().startsWith("java.")) {
                return checkBeanFields(value, expectedType);
            } else {
                return null;
            }
        }

        if (expectedType.equals(Void.TYPE)) {
            // Corner case: void methods return null value by design
            return null;
        }

        if (Void.class.isAssignableFrom(clazz)) {
            // Corner case: explicit Void parameter
            return null;
        }

        if (Optional.class.isAssignableFrom(clazz)) {
            return String.format(
                    "Got null value for type '%s', consider Optional.empty",
                    expectedType.getTypeName());
        }

        return String.format(
                "Got null value for type '%s', which is neither Optional"
                        + " nor void",
                expectedType.getTypeName());
    }

    private String checkIterable(Iterable<?> value, Type expectedType,
            @Nullable List<AnnotatedType> annotatedTypes,
            boolean requiredByContext) {
        Type itemType = Object.class;
        List<AnnotatedType> annotatedItemTypes = List.of();
        String iterableDescription = "iterable";
        if (expectedType instanceof ParameterizedType) {
            itemType = ((ParameterizedType) expectedType)
                    .getActualTypeArguments()[0];
            annotatedItemTypes = getAnnotatedTypeArguments(annotatedTypes, 0);
            iterableDescription = "collection";
        } else if (expectedType instanceof Class<?>) {
            var expectedClass = ((Class<?>) expectedType);

            if (Iterable.class.isAssignableFrom(expectedClass)) {
                // Let's deal with classes extending or implementing an iterator
                itemType = Generics.getExactIterableType(expectedClass)
                        .orElse(Object.class);
                iterableDescription = "collection";
            } else {
                itemType = expectedClass.getComponentType();
                annotatedItemTypes = getAnnotatedComponentTypes(annotatedTypes);
                iterableDescription = "array";
            }
        }

        for (Object item : value) {
            if (item == null && !isNestedValueRequired(annotatedTypes,
                    annotatedItemTypes, requiredByContext)) {
                continue;
            }
            String error = checkValueForType(item, itemType, annotatedItemTypes,
                    requiredByContext);
            if (error != null) {
                return String.format("Unexpected null item in %s type '%s'. %s",
                        iterableDescription, expectedType, error);
            }
        }

        return null;
    }

    private String checkMapValues(Map<?, ?> value, Type expectedType,
            @Nullable List<AnnotatedType> annotatedTypes,
            boolean requiredByContext) {
        Type valueType = Object.class;
        List<AnnotatedType> annotatedValueTypes = List.of();

        if (expectedType instanceof ParameterizedType) {
            valueType = ((ParameterizedType) expectedType)
                    .getActualTypeArguments()[1];
            annotatedValueTypes = getAnnotatedTypeArguments(annotatedTypes, 1);
        }

        for (Map.Entry<?, ?> e : value.entrySet()) {
            if (e.getValue() == null && !isNestedValueRequired(annotatedTypes,
                    annotatedValueTypes, requiredByContext)) {
                continue;
            }
            String error = checkValueForType(e.getValue(), valueType,
                    annotatedValueTypes, requiredByContext);
            if (error != null) {
                return String.format(
                        "Unexpected null value for key '%s' of "
                                + "map type '%s'. %s",
                        e.getKey(), expectedType, error);
            }
        }

        return null;
    }

    private static List<AnnotatedType> getAnnotatedTypeArguments(
            @Nullable List<AnnotatedType> annotatedTypes, int index) {
        if (annotatedTypes == null) {
            return List.of();
        }
        return annotatedTypes.stream()
                .filter(AnnotatedParameterizedType.class::isInstance)
                .map(type -> ((AnnotatedParameterizedType) type)
                        .getAnnotatedActualTypeArguments()[index])
                .toList();
    }

    private static List<AnnotatedType> getAnnotatedComponentTypes(
            @Nullable List<AnnotatedType> annotatedTypes) {
        if (annotatedTypes == null) {
            return List.of();
        }
        return annotatedTypes.stream()
                .filter(AnnotatedArrayType.class::isInstance)
                .map(type -> ((AnnotatedArrayType) type)
                        .getAnnotatedGenericComponentType())
                .toList();
    }

    /**
     * Checks if a nested value, such as a collection item, is required. Without
     * the annotated types of the container, nested values are always required.
     */
    private static boolean isNestedValueRequired(
            @Nullable List<AnnotatedType> annotatedTypes,
            List<AnnotatedType> annotatedNestedTypes,
            boolean requiredByContext) {
        if (annotatedTypes == null) {
            return true;
        }
        return isRequired(
                annotatedNestedTypes.stream()
                        .flatMap(type -> Stream.of(type.getAnnotations())),
                requiredByContext);
    }

    private String checkBeanFields(Object value, Type expectedType) {
        if (hasVisited(value, expectedType)) {
            return null;
        }
        markAsVisited(value, expectedType);
        Class<?> clazz = (Class<?>) expectedType;
        // the context is the package where the object is defined
        boolean requiredByContext = clazz.getPackage() != null && (clazz
                .getPackage().isAnnotationPresent(NonNullApi.class)
                || clazz.getPackage().isAnnotationPresent(NullMarked.class));
        try {
            for (PropertyDescriptor propertyDescriptor : Introspector
                    .getBeanInfo(clazz).getPropertyDescriptors()) {
                if (!isPropertySubjectForChecking(propertyDescriptor,
                        requiredByContext)) {
                    continue;
                }

                Method readMethod = propertyDescriptor.getReadMethod();
                Type propertyType = readMethod.getGenericReturnType();
                Object propertyValue = readMethod.invoke(value);

                String error = checkValueForType(propertyValue, propertyType,
                        getAnnotatedPropertyTypes(propertyDescriptor),
                        requiredByContext);
                if (error != null) {
                    return String.format(
                            "Unexpected null value in Java "
                                    + "Bean type '%s' property '%s'. %s",
                            expectedType.getTypeName(),
                            propertyDescriptor.getName(), error);
                }
            }
        } catch (IntrospectionException | InvocationTargetException
                | IllegalAccessException e) {
            getLogger().error(
                    "Cannot check for null property values in Java Bean", e);
            return e.toString();
        }

        return null;
    }

    private boolean isPropertySubjectForChecking(
            PropertyDescriptor propertyDescriptor, boolean requiredByContext) {
        try {
            String name = propertyDescriptor.getName();
            Method readMethod = propertyDescriptor.getReadMethod();
            if (readMethod == null) {
                return false;
            }

            Class<?> declaringClass = readMethod.getDeclaringClass();
            final Set<String> declaredFieldNames = Arrays
                    .stream(declaringClass.getDeclaredFields())
                    .map(Field::getName).collect(Collectors.toSet());
            if (!declaredFieldNames.contains(name)) {
                return false;
            }

            Field field = readMethod.getDeclaringClass().getDeclaredField(name);
            return (!Modifier.isStatic(field.getModifiers())
                    && !Modifier.isTransient(field.getModifiers())
                    && isPropertyRequired(field, propertyDescriptor,
                            requiredByContext)
                    && !isPropertyIgnored(field, propertyDescriptor));
        } catch (NoSuchFieldException e) {
            getLogger().error("Unexpected missing declared field in Java Bean",
                    e);
            return false;
        }
    }

    /**
     * Checks if a property is left out of the JSON because of
     * {@code @JsonIgnore}: when its field, getter or setter is annotated with
     * {@code @JsonIgnore}, the whole property is left out, unless another of
     * them is annotated with {@code @JsonProperty}. In that case only the
     * ignored ones are left out, so the property is still serialized if its
     * getter, or its field annotated with {@code @JsonProperty}, remains. Other
     * Jackson annotations that include a property are not considered.
     */
    private boolean isPropertyIgnored(Field field,
            PropertyDescriptor propertyDescriptor) {
        Method readMethod = propertyDescriptor.getReadMethod();
        List<AnnotatedElement> accessors = Stream
                .<AnnotatedElement> of(field, readMethod,
                        propertyDescriptor.getWriteMethod())
                .filter(Objects::nonNull).toList();
        if (accessors.stream().noneMatch(this::isJsonIgnored)) {
            return false;
        }
        if (accessors.stream().noneMatch(
                accessor -> accessor.isAnnotationPresent(JsonProperty.class))) {
            return true;
        }
        boolean serializedByGetter = !isJsonIgnored(readMethod);
        boolean serializedByField = !isJsonIgnored(field)
                && field.isAnnotationPresent(JsonProperty.class);
        return !serializedByGetter && !serializedByField;
    }

    private boolean isJsonIgnored(AnnotatedElement accessor) {
        JsonIgnore jsonIgnore = accessor.getAnnotation(JsonIgnore.class);
        return jsonIgnore != null && jsonIgnore.value();
    }

    /**
     * Checks if a property is required, based on the annotations of its field,
     * getter and setter, the same way as in the generated TypeScript.
     */
    private boolean isPropertyRequired(Field field,
            PropertyDescriptor propertyDescriptor, boolean requiredByContext) {
        if (field.getType().isPrimitive()) {
            return true;
        }

        Method writeMethod = propertyDescriptor.getWriteMethod();
        Parameter writeParameter = writeMethod != null
                ? writeMethod.getParameters()[0]
                : null;
        return isRequired(Stream
                .of(field, propertyDescriptor.getReadMethod(), writeParameter)
                .filter(Objects::nonNull).flatMap(
                        ExplicitNullableTypeChecker::getNullabilityAnnotations),
                requiredByContext);
    }

    /**
     * Gets the annotated types which declare the nested values of a property:
     * the getter return type, and the field and setter parameter types when
     * they are the same type as the getter return type.
     */
    private List<AnnotatedType> getAnnotatedPropertyTypes(
            PropertyDescriptor propertyDescriptor) {
        Method readMethod = propertyDescriptor.getReadMethod();
        Type propertyType = readMethod.getGenericReturnType();
        List<AnnotatedType> annotatedTypes = new ArrayList<>();
        annotatedTypes.add(readMethod.getAnnotatedReturnType());

        try {
            Field field = readMethod.getDeclaringClass()
                    .getDeclaredField(propertyDescriptor.getName());
            if (field.getGenericType().equals(propertyType)) {
                annotatedTypes.add(field.getAnnotatedType());
            }
        } catch (NoSuchFieldException e) {
            // a property without a field has no field annotations
        }

        Method writeMethod = propertyDescriptor.getWriteMethod();
        if (writeMethod != null && writeMethod.getGenericParameterTypes()[0]
                .equals(propertyType)) {
            annotatedTypes.add(writeMethod.getAnnotatedParameterTypes()[0]);
        }

        return annotatedTypes;
    }

}
