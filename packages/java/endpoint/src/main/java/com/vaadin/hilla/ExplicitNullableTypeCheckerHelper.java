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
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jspecify.annotations.NullMarked;
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
        return checkValueForType(value, expectedType, List.of());
    }

    /**
     * Validates the given value for the given expected annotated type. Unlike
     * {@link #checkValueForType(Object, Type)}, this also takes the type-use
     * annotations of the type arguments and array components into account, so
     * that, e.g., a {@code null} item is allowed in a
     * {@code List<@Nullable String>}.
     *
     * @param value
     *            the value to validate
     * @param expectedType
     *            the declared annotated type expected for the value
     * @return error message when the value is null while the expected type does
     *         not explicitly allow null, or null meaning the value is OK.
     */
    String checkValueForAnnotatedType(Object value,
            AnnotatedType expectedType) {
        return checkValueForType(value, expectedType.getType(),
                List.of(expectedType));
    }

    /**
     * Validates the given value for the given expected type, taking the
     * nullability annotations of the given annotated types into account. A type
     * is nullable when any of the annotated types marks it so, like the
     * generator does when it combines the field, getter and setter of a
     * property.
     *
     * @param value
     *            the value to validate
     * @param expectedType
     *            the declared type expected for the value
     * @param annotatedTypes
     *            the annotated forms of {@code expectedType} from all its
     *            declarations, or an empty list when no annotations are
     *            available
     * @return error message when the value is null while the expected type does
     *         not explicitly allow null, or null meaning the value is OK.
     */
    String checkValueForType(Object value, Type expectedType,
            List<AnnotatedType> annotatedTypes) {
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
                        annotatedTypes);
            } else if (clazz.isArray() && value instanceof Object[]) {
                return checkIterable(Arrays.asList((Object[]) value),
                        expectedType, annotatedTypes);
            } else if (Map.class.isAssignableFrom(clazz)) {
                return checkMapValues((Map<?, ?>) value, expectedType,
                        annotatedTypes);
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

        if (annotatedTypes.stream()
                .anyMatch(ExplicitNullableTypeChecker::isNullable)) {
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

    private static List<AnnotatedType> getAnnotatedTypeArguments(
            List<AnnotatedType> annotatedTypes, int index) {
        return annotatedTypes.stream()
                .filter(AnnotatedParameterizedType.class::isInstance)
                .map(type -> ((AnnotatedParameterizedType) type)
                        .getAnnotatedActualTypeArguments()[index])
                .toList();
    }

    private String checkIterable(Iterable<?> value, Type expectedType,
            List<AnnotatedType> annotatedTypes) {
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
                annotatedItemTypes = annotatedTypes.stream()
                        .filter(AnnotatedArrayType.class::isInstance)
                        .map(type -> ((AnnotatedArrayType) type)
                                .getAnnotatedGenericComponentType())
                        .toList();
                iterableDescription = "array";
            }
        }

        for (Object item : value) {
            String error = checkValueForType(item, itemType,
                    annotatedItemTypes);
            if (error != null) {
                return String.format("Unexpected null item in %s type '%s'. %s",
                        iterableDescription, expectedType, error);
            }
        }

        return null;
    }

    private String checkMapValues(Map<?, ?> value, Type expectedType,
            List<AnnotatedType> annotatedTypes) {
        Type valueType = Object.class;
        List<AnnotatedType> annotatedValueTypes = List.of();

        if (expectedType instanceof ParameterizedType) {
            valueType = ((ParameterizedType) expectedType)
                    .getActualTypeArguments()[1];
            annotatedValueTypes = getAnnotatedTypeArguments(annotatedTypes, 1);
        }

        for (Map.Entry<?, ?> e : value.entrySet()) {
            String error = checkValueForType(e.getValue(), valueType,
                    annotatedValueTypes);
            if (error != null) {
                return String.format(
                        "Unexpected null value for key '%s' of "
                                + "map type '%s'. %s",
                        e.getKey(), expectedType, error);
            }
        }

        return null;
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
                Object propertyValue = readMethod.invoke(value);

                Type propertyType = readMethod.getGenericReturnType();
                String error = checkValueForType(propertyValue, propertyType,
                        getAnnotatedPropertyTypes(propertyDescriptor,
                                propertyType));
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

    /**
     * Returns the annotated types of the getter, the setter parameter and the
     * field of the property that declare the given type, as the generator
     * combines their nullability annotations too.
     */
    private static List<AnnotatedType> getAnnotatedPropertyTypes(
            PropertyDescriptor propertyDescriptor, Type propertyType) {
        Method readMethod = propertyDescriptor.getReadMethod();
        List<AnnotatedType> annotatedTypes = new ArrayList<>();
        annotatedTypes.add(readMethod.getAnnotatedReturnType());

        Method writeMethod = propertyDescriptor.getWriteMethod();
        if (writeMethod != null) {
            annotatedTypes
                    .add(writeMethod.getParameters()[0].getAnnotatedType());
        }

        try {
            annotatedTypes.add(readMethod.getDeclaringClass()
                    .getDeclaredField(propertyDescriptor.getName())
                    .getAnnotatedType());
        } catch (NoSuchFieldException e) {
            // The property has no field of the same name
        }

        annotatedTypes.removeIf(
                annotatedType -> !propertyType.equals(annotatedType.getType()));
        return annotatedTypes;
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
                    && isRequired(field, requiredByContext)
                    && !field.isAnnotationPresent(JsonIgnore.class));
        } catch (NoSuchFieldException e) {
            getLogger().error("Unexpected missing declared field in Java Bean",
                    e);
            return false;
        }
    }

}
