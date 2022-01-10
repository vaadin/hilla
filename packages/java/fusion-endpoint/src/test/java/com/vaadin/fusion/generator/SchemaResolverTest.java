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
package com.vaadin.fusion.generator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedArrayType;
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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SchemaResolverTest {

    @Test
    public void should_ReturnArraySchema_When_GivenTypeIsAnArray() {
        ResolvedType arrayType = mock(ResolvedType.class);
        ResolvedArrayType arrayResolvedType = mock(ResolvedArrayType.class);
        ResolvedType stringType = mockReferencedTypeOf(String.class);

        when(arrayType.isArray()).thenReturn(true);
        when(arrayType.asArrayType()).thenReturn(arrayResolvedType);
        when(arrayResolvedType.getComponentType()).thenReturn(stringType);

        Map<String, GeneratorType> usedTypes = new HashMap<>();
        SchemaResolver schemaResolver = new SchemaResolver(
                new GeneratorType(arrayType), usedTypes);

        Schema schema = schemaResolver.resolve();
        Assert.assertTrue(schema instanceof ArraySchema);
        Assert.assertTrue(schema.getNullable());
        Assert.assertTrue(
                ((ArraySchema) schema).getItems() instanceof StringSchema);
        Assert.assertTrue(usedTypes.isEmpty());
    }

    @Test
    public void should_ReturnNotNullableNumberSchema_When_GivenTypeIsAPrimitiveInt() {
        ResolvedType numberType = mockPrimitiveTypeOf(
                ResolvedPrimitiveType.INT);

        Map<String, GeneratorType> usedTypes = new HashMap<>();
        SchemaResolver schemaResolver = new SchemaResolver(
                new GeneratorType(numberType), usedTypes);
        Schema schema = schemaResolver.resolve();
        Assert.assertTrue(schema instanceof NumberSchema);
        Assert.assertNull(schema.getNullable());
        Assert.assertTrue(usedTypes.isEmpty());
    }

    @Test
    public void should_ReturnNullableNumberSchema_When_GivenTypeIsANumber() {
        ResolvedType numberType = mockReferencedTypeOf(Number.class);

        Map<String, GeneratorType> usedTypes = new HashMap<>();
        SchemaResolver schemaResolver = new SchemaResolver(
                new GeneratorType(numberType), usedTypes);
        Schema schema = schemaResolver.resolve();
        Assert.assertTrue(schema instanceof NumberSchema);
        Assert.assertTrue(schema.getNullable());
        Assert.assertTrue(usedTypes.isEmpty());
    }

    @Test
    public void should_ReturnNullableStringSchema_When_GivenTypeIsAString() {
        ResolvedType resolvedType = mockReferencedTypeOf(String.class);
        Map<String, GeneratorType> usedTypes = new HashMap<>();
        SchemaResolver schemaResolver = new SchemaResolver(
                new GeneratorType(resolvedType), usedTypes);
        Schema schema = schemaResolver.resolve();

        Assert.assertTrue(schema instanceof StringSchema);
        Assert.assertTrue(schema.getNullable());
        Assert.assertTrue(usedTypes.isEmpty());
    }

    @Test
    public void should_ReturnNullableArray_When_GivenTypeIsAListString() {
        ResolvedType resolvedType = mockReferencedTypeOf(Collection.class);
        ResolvedReferenceType resolvedReferenceType = resolvedType
                .asReferenceType();

        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> pairs = Collections
                .singletonList(
                        new Pair<>(null, mockReferencedTypeOf(String.class)));
        when(resolvedReferenceType.getTypeParametersMap()).thenReturn(pairs);

        Map<String, GeneratorType> usedTypes = new HashMap<>();
        SchemaResolver schemaResolver = new SchemaResolver(
                new GeneratorType(resolvedType), usedTypes);
        Schema schema = schemaResolver.resolve();

        Assert.assertTrue(schema instanceof ArraySchema);
        Assert.assertTrue(schema.getNullable());
        Assert.assertTrue(
                ((ArraySchema) schema).getItems() instanceof StringSchema);
        Assert.assertTrue(usedTypes.isEmpty());
    }

    @Test
    public void should_ReturnNotNullableBoolean_When_GivenTypeIsAPrimitiveBoolean() {
        ResolvedType resolvedType = mockPrimitiveTypeOf(
                ResolvedPrimitiveType.BOOLEAN);
        Map<String, GeneratorType> usedTypes = new HashMap<>();
        SchemaResolver schemaResolver = new SchemaResolver(
                new GeneratorType(resolvedType), usedTypes);
        Schema schema = schemaResolver.resolve();

        Assert.assertTrue(schema instanceof BooleanSchema);
        Assert.assertNull(schema.getNullable());
        Assert.assertTrue(usedTypes.isEmpty());
    }

    @Test
    public void should_ReturnNullableBoolean_When_GivenTypeIsABoxedBoolean() {
        ResolvedType resolvedType = mockReferencedTypeOf(Boolean.class);
        Map<String, GeneratorType> usedTypes = new HashMap<>();
        SchemaResolver schemaResolver = new SchemaResolver(
                new GeneratorType(resolvedType), usedTypes);
        Schema schema = schemaResolver.resolve();

        Assert.assertTrue(schema instanceof BooleanSchema);
        Assert.assertTrue(schema.getNullable());
        Assert.assertTrue(usedTypes.isEmpty());
    }

    @Test
    public void should_ReturnNullableMap_When_GivenTypeIsAMap() {
        Type type = mock(Type.class);
        ResolvedType resolvedType = mockReferencedTypeOf(Map.class);
        Mockito.doReturn(resolvedType).when(type).resolve();

        GeneratorType generatorType = Mockito
                .spy(new GeneratorType(resolvedType));
        Mockito.doReturn(Arrays.asList(null,
                new GeneratorType(mockReferencedTypeOf(Number.class))))
                .when(generatorType).getTypeArguments();

        Map<String, GeneratorType> usedTypes = new HashMap<>();
        SchemaResolver schemaResolver = new SchemaResolver(generatorType,
                usedTypes);
        Schema schema = schemaResolver.resolve();

        Assert.assertTrue(schema instanceof MapSchema);
        Assert.assertTrue(schema.getNullable());
        Assert.assertTrue(
                schema.getAdditionalProperties() instanceof NumberSchema);
        Assert.assertTrue(usedTypes.isEmpty());
    }

    @Test
    public void should_ReturnNullableDate_When_GivenTypeIsADate() {
        ResolvedType resolvedType = mockReferencedTypeOf(Date.class);
        Map<String, GeneratorType> usedTypes = new HashMap<>();
        SchemaResolver schemaResolver = new SchemaResolver(
                new GeneratorType(resolvedType), usedTypes);
        Schema schema = schemaResolver.resolve();

        Assert.assertTrue(schema instanceof DateSchema);
        Assert.assertTrue(schema.getNullable());
        Assert.assertTrue(usedTypes.isEmpty());
    }

    @Test
    public void should_ReturnNullableDate_When_GivenTypeIsAInstant() {
        ResolvedType resolvedType = mockReferencedTypeOf(Instant.class);
        Map<String, GeneratorType> usedTypes = new HashMap<>();
        SchemaResolver schemaResolver = new SchemaResolver(
                new GeneratorType(resolvedType), usedTypes);
        Schema schema = schemaResolver.resolve();

        Assert.assertTrue(schema instanceof DateTimeSchema);
        Assert.assertTrue(schema.getNullable());
        Assert.assertTrue(usedTypes.isEmpty());
    }

    @Test
    public void should_ReturnNullableOptional_When_GivenTypeIsAnOptionalString() {
        ResolvedType resolvedType = mockReferencedTypeOf(Optional.class);
        ResolvedReferenceType resolvedReferenceType = resolvedType
                .asReferenceType();

        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> pairs = Collections
                .singletonList(
                        new Pair<>(null, mockReferencedTypeOf(String.class)));
        when(resolvedReferenceType.getTypeParametersMap()).thenReturn(pairs);

        Map<String, GeneratorType> usedTypes = new HashMap<>();
        SchemaResolver schemaResolver = new SchemaResolver(
                new GeneratorType(resolvedType), usedTypes);
        Schema schema = schemaResolver.resolve();

        Assert.assertTrue(schema instanceof StringSchema);
        Assert.assertTrue(schema.getNullable());
        Assert.assertTrue(usedTypes.isEmpty());
    }

    @Test
    public void should_ReturnNullableOptional_When_GivenTypeIsAnOptionalBean() {
        Type type = mock(Type.class);
        ResolvedType resolvedType = mockReferencedTypeOf(Optional.class);
        Mockito.doReturn(resolvedType).when(type).resolve();

        GeneratorType generatorType = Mockito.spy(new GeneratorType(type));
        Mockito.doReturn(Arrays.asList(
                new GeneratorType(mockReferencedTypeOf(TestBean.class))))
                .when(generatorType).getTypeArguments();

        Map<String, GeneratorType> usedTypes = new HashMap<>();
        SchemaResolver schemaResolver = new SchemaResolver(generatorType,
                usedTypes);
        Schema schema = schemaResolver.resolve();

        Assert.assertTrue(schema instanceof ComposedSchema);
        Assert.assertTrue(schema.getNullable());
        Assert.assertEquals(1, ((ComposedSchema) schema).getAllOf().size());
        String beanRef = schemaResolver
                .getFullQualifiedNameRef(TestBean.class.getCanonicalName());
        Assert.assertEquals(beanRef,
                ((ComposedSchema) schema).getAllOf().get(0).get$ref());

        Assert.assertEquals(1, usedTypes.size());
    }

    @Test
    public void should_ReturnNullableObject_When_GivenTypeIsAnUnhandledJavaType() {
        ResolvedType resolvedType = mockReferencedTypeOf(Class.class);
        Map<String, GeneratorType> usedTypes = new HashMap<>();
        SchemaResolver schemaResolver = new SchemaResolver(
                new GeneratorType(resolvedType), usedTypes);
        Schema schema = schemaResolver.resolve();

        Assert.assertNotNull(schema);
        Assert.assertTrue(schema.getNullable());
        Assert.assertTrue(usedTypes.isEmpty());
    }

    @Test
    public void should_ReturnNullableBeanSchema_When_GivenTypeIsABeanType() {
        ResolvedType resolvedType = mockReferencedTypeOf(TestBean.class);
        Type type = mock(Type.class);
        Mockito.doReturn(resolvedType).when(type).resolve();
        Map<String, GeneratorType> usedTypes = new HashMap<>();
        SchemaResolver schemaResolver = new SchemaResolver(
                new GeneratorType(type), usedTypes);

        Schema schema = schemaResolver.resolve();

        Assert.assertTrue(schema instanceof ComposedSchema);
        Assert.assertEquals(1, ((ComposedSchema) schema).getAllOf().size());
        Assert.assertTrue(((ComposedSchema) schema).getAllOf()
                .get(0) instanceof ObjectSchema);
        ;
        Assert.assertTrue(schema.getNullable());
        String beanRef = schemaResolver
                .getFullQualifiedNameRef(TestBean.class.getCanonicalName());
        Assert.assertEquals(beanRef,
                ((ComposedSchema) schema).getAllOf().get(0).get$ref());

        Assert.assertEquals(1, usedTypes.size());
    }

    @Test
    public void should_ReturnBeanSchema_When_GivenTypeIsABeanImplementingIterable() {
        ResolvedType resolvedType = mockReferencedTypeOf(
                TestIterableBean.class);
        Type type = mock(Type.class);
        Mockito.doReturn(resolvedType).when(type).resolve();
        Map<String, GeneratorType> usedTypes = new HashMap<>();

        SchemaResolver schemaResolver = new SchemaResolver(
                new GeneratorType(type), usedTypes);
        Schema schema = schemaResolver.resolve();
        Assert.assertTrue(schema instanceof ComposedSchema);
        Assert.assertEquals(1, ((ComposedSchema) schema).getAllOf().size());
        Assert.assertTrue(((ComposedSchema) schema).getAllOf()
                .get(0) instanceof ObjectSchema);
        String beanRef = schemaResolver.getFullQualifiedNameRef(
                TestIterableBean.class.getCanonicalName());
        Assert.assertEquals(beanRef,
                ((ComposedSchema) schema).getAllOf().get(0).get$ref());
        Assert.assertEquals(1, usedTypes.size());
    }

    private ResolvedType mockReferencedTypeOf(Class<?> clazz) {
        ResolvedType resolvedType = mock(ResolvedType.class);
        ResolvedReferenceType resolvedReferenceType = mock(
                ResolvedReferenceType.class);

        when(resolvedType.isPrimitive()).thenReturn(false);
        when(resolvedType.isReferenceType()).thenReturn(true);
        when(resolvedType.asReferenceType()).thenReturn(resolvedReferenceType);
        when(resolvedReferenceType.getQualifiedName())
                .thenReturn(clazz.getCanonicalName());
        List<ResolvedReferenceType> ancestors = new ArrayList<>();
        for (Class<?> c : clazz.getInterfaces()) {
            ancestors.add(mockReferencedTypeOf(c).asReferenceType());
        }
        if (clazz.getSuperclass() != null) {
            ancestors.add(mockReferencedTypeOf(clazz.getSuperclass())
                    .asReferenceType());
        }
        when(resolvedReferenceType.getAllAncestors()).thenReturn(ancestors);
        return resolvedType;
    }

    private ResolvedType mockPrimitiveTypeOf(
            ResolvedPrimitiveType primitiveType) {
        ResolvedType numberType = mock(ResolvedType.class);

        when(numberType.isPrimitive()).thenReturn(true);
        when(numberType.asPrimitive()).thenReturn(primitiveType);
        return numberType;
    }

    private static class TestBean {
        String foo;
    }

    private static class TestIterableBean implements Iterable<String> {
        String foo;

        @Override
        public Iterator<String> iterator() {
            return Collections.emptyIterator();
        }
    }

}
