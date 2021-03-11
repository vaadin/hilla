/*
 * Copyright 2000-2020 Vaadin Ltd.
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
package com.vaadin.flow.server.connect.generator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.resolution.declarations.ResolvedEnumConstantDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.Pair;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

class SchemaResolver {

    private static final String SCHEMA_REF_PREFIX = "#/components/schemas/";
    private final Map<String, ResolvedReferenceType> foundTypes = new HashMap<>();

    Schema parseResolvedTypeToSchema(ResolvedType resolvedType) {
        if (resolvedType.isArray()) {
            return createArraySchema(resolvedType);
        }
        if (isNumberType(resolvedType)) {
            return new NumberSchema();
        } else if (isStringType(resolvedType)) {
            return new StringSchema();
        } else if (isCollectionType(resolvedType)) {
            return createCollectionSchema(resolvedType.asReferenceType());
        } else if (isBooleanType(resolvedType)) {
            return new BooleanSchema();
        } else if (isMapType(resolvedType)) {
            return createMapSchema(resolvedType);
        } else if (isDateType(resolvedType)) {
            return new DateSchema();
        } else if (isDateTimeType(resolvedType)) {
            return new DateTimeSchema();
        } else if (isOptionalType(resolvedType)) {
            return createOptionalSchema(resolvedType.asReferenceType());
        } else if (isUnhandledJavaType(resolvedType)) {
            return new ObjectSchema();
        } else if (isTypeOf(resolvedType, Enum.class)) {
            return createEnumTypeSchema(resolvedType);
        }
        return createUserBeanSchema(resolvedType);
    }

    private Schema createArraySchema(ResolvedType type) {
        ArraySchema array = new ArraySchema();
        array.items(parseResolvedTypeToSchema(
                type.asArrayType().getComponentType()));
        return array;
    }

    private Schema createCollectionSchema(ResolvedReferenceType type) {
        ArraySchema array = new ArraySchema();
        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap = type
                .getTypeParametersMap();
        if (!typeParametersMap.isEmpty()) {
            ResolvedType collectionParameterType = typeParametersMap.get(0).b;
            array.items(parseResolvedTypeToSchema(collectionParameterType));
        }
        return array;
    }

    private Schema createOptionalSchema(ResolvedReferenceType type) {
        ResolvedType typeInOptional = type.getTypeParametersMap().get(0).b;
        Schema nestedTypeSchema = parseResolvedTypeToSchema(typeInOptional);
        return createNullableWrapper(nestedTypeSchema);
    }

    Schema createNullableWrapper(Schema nestedTypeSchema) {
        if (nestedTypeSchema.get$ref() == null) {
            nestedTypeSchema.setNullable(true);
            return nestedTypeSchema;
        } else {
            ComposedSchema nullableSchema = new ComposedSchema();
            nullableSchema.setNullable(true);
            nullableSchema.setAllOf(Collections.singletonList(nestedTypeSchema));
            return nullableSchema;
        }
    }

    private Schema createMapSchema(ResolvedType type) {
        Schema mapSchema = new MapSchema();
        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap = type
                .asReferenceType().getTypeParametersMap();
        if (typeParametersMap.size() == 2) {
            // Assumed that Map always has the first type parameter as `String`
            // and
            // the second is for its value type
            ResolvedType mapValueType = typeParametersMap.get(1).b;
            mapSchema.additionalProperties(
                    parseResolvedTypeToSchema(mapValueType));
        }
        return mapSchema;
    }

    private boolean isOptionalType(ResolvedType resolvedType) {
        return resolvedType.isReferenceType()
                && isTypeOf(resolvedType, Optional.class);
    }

    private boolean isUnhandledJavaType(ResolvedType resolvedType) {
        return resolvedType.isReferenceType() && resolvedType.asReferenceType()
                .getQualifiedName().startsWith("java.");
    }

    private boolean isDateTimeType(ResolvedType resolvedType) {
        return resolvedType.isReferenceType()
                && isTypeOf(resolvedType, LocalDateTime.class, Instant.class, LocalTime.class);
    }

    private boolean isDateType(ResolvedType resolvedType) {
        return resolvedType.isReferenceType()
                && isTypeOf(resolvedType, Date.class, LocalDate.class);
    }

    private boolean isNumberType(ResolvedType type) {
        if (type.isPrimitive()) {
            ResolvedPrimitiveType resolvedPrimitiveType = type.asPrimitive();
            return resolvedPrimitiveType != ResolvedPrimitiveType.BOOLEAN
                    && resolvedPrimitiveType != ResolvedPrimitiveType.CHAR;
        } else {
            return isTypeOf(type, Number.class);
        }
    }

    private boolean isCollectionType(ResolvedType type) {
        return !type.isPrimitive() && (isTypeOf(type, Collection.class) || isType(type, Iterable.class));
    }

    private boolean isMapType(ResolvedType type) {
        return !type.isPrimitive() && isTypeOf(type, Map.class);
    }

    private boolean isBooleanType(ResolvedType type) {
        if (type.isPrimitive()) {
            return type.asPrimitive() == ResolvedPrimitiveType.BOOLEAN;
        } else {
            return isTypeOf(type, Boolean.class);
        }
    }

    private boolean isStringType(ResolvedType type) {
        if (type.isPrimitive()) {
            return type.asPrimitive() == ResolvedPrimitiveType.CHAR;
        } else {
            return isTypeOf(type, String.class, Character.class);
        }
    }

    /**
     * Checks if the given type refers to the given class.
     *
     * @param type
     *            type type to check
     * @param clazz
     *            the class to match with
     * @return true if the type is referring to the given class, false otherwise
     */
    private boolean isType(ResolvedType type, Class<?> clazz) {
        if (!type.isReferenceType()) {
            return false;
        }
        return clazz.getName().equals(type.asReferenceType().getQualifiedName());
    }

    /**
     * Checks if the given type can be cast to one of the given classes.
     *
     * @param type
     *            type type to check
     * @param clazz
     *            the classes to match with
     * @return true if the type can be cast to one of the given classes, false otherwise
     */
    private boolean isTypeOf(ResolvedType type, Class<?>... clazz) {
        if (!type.isReferenceType()) {
            return false;
        }
        List<String> classes = Arrays.stream(clazz).map(Class::getName)
                .collect(Collectors.toList());
        return classes.contains(type.asReferenceType().getQualifiedName())
                || type.asReferenceType().getAllAncestors().stream()
                        .map(ResolvedReferenceType::getQualifiedName)
                        .anyMatch(classes::contains);
    }

    private Schema createEnumTypeSchema(ResolvedType resolvedType) {
        ResolvedReferenceType type = resolvedType.asReferenceType();
        List<String> entries = type
                .getTypeDeclaration().asEnum().getEnumConstants().stream()
                .map(ResolvedEnumConstantDeclaration::getName)
                .collect(Collectors.toList());
        String qualifiedName = type.getQualifiedName();
        foundTypes.put(qualifiedName, type);
        StringSchema schema = new StringSchema();
        schema.name(qualifiedName);
        schema.setEnum(entries);
        schema.$ref(getFullQualifiedNameRef(qualifiedName));
        return schema;
    }

    private Schema createUserBeanSchema(ResolvedType resolvedType) {
        if (resolvedType.isReferenceType()) {
            String qualifiedName = resolvedType.asReferenceType()
                    .getQualifiedName();
            foundTypes.put(qualifiedName, resolvedType.asReferenceType());
            return new ObjectSchema().name(qualifiedName)
                    .$ref(getFullQualifiedNameRef(qualifiedName));
        }
        return new ObjectSchema();
    }

    /**
     * This method is needed because the {@link Schema#set$ref(String)} method
     * won't append "#/components/schemas/" if the ref contains `.`.
     *
     * @param qualifiedName
     *            full qualified name of the class
     * @return the ref in format of "#/components/schemas/com.my.example.Model"
     */
    String getFullQualifiedNameRef(String qualifiedName) {
        return SCHEMA_REF_PREFIX + qualifiedName;
    }

    String getSimpleRef(String ref) {
        if (GeneratorUtils.contains(ref, SCHEMA_REF_PREFIX)) {
            return GeneratorUtils.substringAfter(ref, SCHEMA_REF_PREFIX);
        }
        return ref;
    }

    void addFoundTypes(String qualifiedName, ResolvedReferenceType type) {
        foundTypes.put(qualifiedName, type);
    }

    ResolvedReferenceType getFoundTypeByQualifiedName(String qualifiedName) {
        return foundTypes.get(qualifiedName);
    }

    Map<String, ResolvedReferenceType> getFoundTypes() {
        return foundTypes;
    }
}
