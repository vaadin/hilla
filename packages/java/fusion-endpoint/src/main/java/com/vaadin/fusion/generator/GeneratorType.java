/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.fusion.generator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.Pair;

import com.vaadin.fusion.ExplicitNullableTypeChecker;

import static com.vaadin.fusion.generator.GeneratorUtils.zip;

class GeneratorType {
    private final Type type;
    private final ResolvedType resolvedType;
    private final boolean isResolvable;

    GeneratorType(Type type) {
        this.type = type;
        this.resolvedType = type.resolve();
        isResolvable = true;
    }

    GeneratorType(ResolvedType resolvedType) {
        this.type = null;
        this.resolvedType = resolvedType;
        isResolvable = true;
    }

    GeneratorType(Type type, ResolvedType resolvedType) {
        this.type = type;
        this.resolvedType = resolvedType;
        isResolvable = false;
    }

    boolean hasType() {
        return type != null;
    }

    boolean isArray() {
        return resolvedType.isArray();
    }

    boolean isBoolean() {
        if (resolvedType.isPrimitive()) {
            return resolvedType.asPrimitive() == ResolvedPrimitiveType.BOOLEAN;
        }

        return isAssignableType(Boolean.class);
    }

    boolean isCollection() {
        return !resolvedType.isPrimitive()
                && (isAssignableType(Collection.class)
                        || isExactType(Iterable.class));
    }

    boolean isDate() {
        return resolvedType.isReferenceType()
                && isAssignableType(Date.class, LocalDate.class);
    }

    boolean isDateTime() {
        return resolvedType.isReferenceType() && isAssignableType(
                LocalDateTime.class, Instant.class, LocalTime.class);
    }

    boolean isEnum() {
        return isAssignableType(Enum.class);
    }

    boolean isMap() {
        return !resolvedType.isPrimitive() && isAssignableType(Map.class);
    }

    boolean isNumber() {
        if (resolvedType.isPrimitive()) {
            ResolvedPrimitiveType resolvedPrimitiveType = resolvedType
                    .asPrimitive();
            return resolvedPrimitiveType != ResolvedPrimitiveType.BOOLEAN
                    && resolvedPrimitiveType != ResolvedPrimitiveType.CHAR;
        } else {
            return isAssignableType(Number.class);
        }
    }

    boolean isOptional() {
        return resolvedType.isReferenceType()
                && isAssignableType(Optional.class);
    }

    boolean isPrimitive() {
        return resolvedType.isPrimitive();
    }

    boolean isReference() {
        return resolvedType.isReferenceType();
    }

    boolean isRequired() {
        return isPrimitive() || hasType() && ExplicitNullableTypeChecker
                .isRequired(type.getAnnotations());
    }

    boolean isString() {
        if (resolvedType.isPrimitive()) {
            return resolvedType.asPrimitive() == ResolvedPrimitiveType.CHAR;
        }

        return isAssignableType(String.class, Character.class);
    }

    boolean isUnhandled() {
        return resolvedType.isReferenceType() && resolvedType.asReferenceType()
                .getQualifiedName().startsWith("java.");
    }

    /**
     * Checks if the given type refers to the given class.
     *
     * @param classes
     *            the class to match with
     * @return true if the type is referring to the given class, false otherwise
     */
    boolean isExactType(Class<?>... classes) {
        return resolvedType.isReferenceType()
                && isType(resolvedType.asReferenceType(), classes);
    }

    /**
     * Checks if the given type can be cast to one of the given classes.
     *
     * @param classes
     *            the classes to match with
     * @return true if the type can be cast to one of the given classes, false
     *         otherwise
     */
    boolean isAssignableType(Class<?>... classes) {
        if (!resolvedType.isReferenceType()) {
            return false;
        }

        ResolvedReferenceType resolvedReferenceType = resolvedType
                .asReferenceType();

        return isType(resolvedReferenceType, classes)
                || resolvedReferenceType.getAllAncestors().stream()
                        .anyMatch(ancestor -> isType(ancestor, classes));
    }

    ResolvedType asResolvedType() {
        return resolvedType;
    }

    Optional<Type> asType() {
        return Optional.ofNullable(type);
    }

    GeneratorType getItemType() {
        if (hasType()) {
            Type componentType = type.asArrayType().getComponentType();

            if (isResolvable) {
                return new GeneratorType(componentType);
            }

            return new GeneratorType(componentType,
                    resolvedType.asArrayType().getComponentType());
        }

        return new GeneratorType(resolvedType.asArrayType().getComponentType());
    }

    List<GeneratorType> getTypeArguments() {
        if (hasType()) {
            return type.asClassOrInterfaceType().getTypeArguments()
                    .map(typeArguments -> {
                        if (isResolvable) {
                            return typeArguments.stream()
                                    .map(GeneratorType::new)
                                    .collect(Collectors.toList());
                        }

                        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParameters = resolvedType
                                .asReferenceType().getTypeParametersMap();

                        return zip(typeArguments, typeParameters,
                                (argument, parameterPair) -> new GeneratorType(
                                        argument, parameterPair.b))
                                                .collect(Collectors.toList());
                    }).orElseGet(this::getTypeArgumentsFallback);
        }

        return getTypeArgumentsFallback();
    }

    private List<GeneratorType> getTypeArgumentsFallback() {
        return resolvedType.asReferenceType().getTypeParametersMap().stream()
                .filter(Objects::nonNull)
                .map(parameter -> new GeneratorType(parameter.b))
                .collect(Collectors.toList());
    }

    private boolean isType(ResolvedReferenceType type, Class<?>... classes) {
        return Arrays.stream(classes).map(Class::getName).anyMatch(
                className -> className.equals(type.getQualifiedName()));
    }
}
