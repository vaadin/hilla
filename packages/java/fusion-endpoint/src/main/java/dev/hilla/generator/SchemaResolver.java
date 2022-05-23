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
package dev.hilla.generator;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedEnumConstantDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;

import dev.hilla.ExplicitNullableTypeChecker;
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
    private final Map<String, GeneratorType> usedTypes;
    private final GeneratorType type;
    private final List<AnnotationExpr> nodeAnnotations;
    private boolean requiredByContext;

    SchemaResolver(GeneratorType type, Map<String, GeneratorType> usedTypes,
            boolean requiredByContext) {
        this(type, null, usedTypes, requiredByContext);
    }

    SchemaResolver(GeneratorType type, List<AnnotationExpr> nodeAnnotations,
            Map<String, GeneratorType> usedTypes, boolean requiredByContext) {
        this.type = type;
        this.nodeAnnotations = nodeAnnotations;
        this.usedTypes = usedTypes;
        this.requiredByContext = requiredByContext;
    }

    /**
     * This method is needed because the {@link Schema#set$ref(String)} method
     * won't append "#/components/schemas/" if the ref contains `.`.
     *
     * @param qualifiedName
     *            full qualified name of the class
     * @return the ref in format of "#/components/schemas/com.my.example.Model"
     */
    static String getFullQualifiedNameRef(String qualifiedName) {
        return SCHEMA_REF_PREFIX + qualifiedName;
    }

    static String getSimpleRef(String ref) {
        if (GeneratorUtils.contains(ref, SCHEMA_REF_PREFIX)) {
            return GeneratorUtils.substringAfter(ref, SCHEMA_REF_PREFIX);
        }
        return ref;
    }

    private static Schema createNullableWrapper(Schema nestedTypeSchema,
            boolean shouldBeNullable) {
        if (!shouldBeNullable) {
            return nestedTypeSchema;
        }

        if (nestedTypeSchema.get$ref() == null) {
            nestedTypeSchema.setNullable(true);
            return nestedTypeSchema;
        }

        ComposedSchema nullableSchema = new ComposedSchema();
        nullableSchema.setNullable(true);
        nullableSchema.setAllOf(Collections.singletonList(nestedTypeSchema));
        return nullableSchema;
    }

    Schema resolve() {
        if (type.isArray()) {
            return createNullableWrapper(createArraySchema());
        }

        if (type.isNumber()) {
            return createNullableWrapper(new NumberSchema());
        }

        if (type.isString()) {
            return createNullableWrapper(new StringSchema());
        }

        if (type.isCollection()) {
            return createNullableWrapper(createCollectionSchema());
        }

        if (type.isBoolean()) {
            return createNullableWrapper(new BooleanSchema());
        }

        if (type.isMap()) {
            return createNullableWrapper(createMapSchema());
        }

        if (type.isDate()) {
            return createNullableWrapper(new DateSchema());
        }

        if (type.isDateTime()) {
            return createNullableWrapper(new DateTimeSchema());
        }

        if (type.isOptional()) {
            return createOptionalSchema();
        }

        if (type.isUnhandled()) {
            return createNullableWrapper(new ObjectSchema());
        }

        if (type.isEnum()) {
            return createNullableWrapper(createEnumTypeSchema());
        }

        if (type.isFlux()) {
            return createNullableWrapper(createFluxSchema());
        }

        return createNullableWrapper(createUserBeanSchema());
    }

    private Schema createArraySchema() {
        ArraySchema array = new ArraySchema();
        array.items(new SchemaResolver(type.getItemType(), usedTypes,
                requiredByContext).resolve());
        return array;
    }

    private Schema createCollectionSchema() {
        ArraySchema array = new ArraySchema();
        List<GeneratorType> typeArguments = type.getTypeArguments();

        if (!typeArguments.isEmpty()) {
            array.items(new SchemaResolver(typeArguments.get(0), usedTypes,
                    requiredByContext).resolve());
        }

        return array;
    }

    private Schema createOptionalSchema() {
        return createNullableWrapper(
                new SchemaResolver(type.getTypeArguments().get(0), usedTypes,
                        requiredByContext).resolve());
    }

    private Schema createNullableWrapper(Schema nestedTypeSchema) {
        return createNullableWrapper(nestedTypeSchema, !isRequired());
    }

    private Schema createMapSchema() {
        Schema mapSchema = new MapSchema();
        List<GeneratorType> typeArguments = type.getTypeArguments();

        if (typeArguments.size() == 2) {
            // Assumed that Map always has the first type parameter as `String`
            // and the second is for its value type
            mapSchema.additionalProperties(
                    new SchemaResolver(typeArguments.get(1), usedTypes,
                            requiredByContext).resolve());
        }
        return mapSchema;
    }

    private Schema createEnumTypeSchema() {
        ResolvedReferenceType resolvedReferenceType = type.asResolvedType()
                .asReferenceType();
        List<String> entries = resolvedReferenceType.getTypeDeclaration()
                .orElseThrow(IllegalArgumentException::new).asEnum()
                .getEnumConstants().stream()
                .map(ResolvedEnumConstantDeclaration::getName)
                .collect(Collectors.toList());
        String qualifiedName = resolvedReferenceType.getQualifiedName();
        usedTypes.put(qualifiedName, type);
        StringSchema schema = new StringSchema();
        schema.name(qualifiedName);
        schema.setEnum(entries);
        schema.$ref(getFullQualifiedNameRef(qualifiedName));
        return schema;
    }

    private Schema createUserBeanSchema() {
        if (type.isReference()) {
            ResolvedReferenceType resolvedReferenceType = type.asResolvedType()
                    .asReferenceType();
            String qualifiedName = resolvedReferenceType.getQualifiedName();
            usedTypes.put(qualifiedName, type);
            return new ObjectSchema().name(qualifiedName)
                    .$ref(getFullQualifiedNameRef(qualifiedName));
        }
        return new ObjectSchema();
    }

    private Schema createFluxSchema() {
        Schema subTypeSchema = new SchemaResolver(
                type.getTypeArguments().get(0), usedTypes, requiredByContext)
                        .resolve();
        ArraySchema arr = new ArraySchema();
        arr.setItems(subTypeSchema);
        arr.addExtension("x-flux", true);
        return arr;
    }

    private boolean isRequired() {
        // Method return value annotations are not included in the type but need
        // to be passed separately
        return type.isRequired(nodeAnnotations);
    }
}
