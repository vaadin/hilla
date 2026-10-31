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
package com.vaadin.hilla.parser.models;

import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredFields;
import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.Field;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.vaadin.hilla.parser.test.helpers.SpecializationChecker;
import com.vaadin.hilla.parser.test.helpers.context.AbstractContext;

public class TypeVariableModelTests {
    private Context ctx;

    @BeforeEach
    public void setUp() {
        ctx = new Context();
    }

    @DisplayName("It should get annotations")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetAnnotations(TypeVariableModel model, String name) {
        assertEquals(List.of(Sample.Foo.class.getName()),
                model.getAnnotations().stream()
                        .map(AnnotationInfoModel::getName)
                        .collect(Collectors.toList()));
    }

    @DisplayName("It should resolve to type parameters")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetResolveToTypeParameters(TypeVariableModel model,
            String name) {
        assertEquals(TypeParameterModel.of(ctx.getParameter(name)),
                model.resolve());
    }

    @DisplayName("It should have the same hashCode for models of the same origin")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveSameHashCodeForModelsOfSameOrigin(
            TypeVariableModel model, TypeVariableModel other, String name) {
        assertEquals(model.hashCode(), other.hashCode());
    }

    @DisplayName("It should have models of the same origin equal")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveModelsOfSameOriginEqual(TypeVariableModel model,
            TypeVariableModel other, String name) {
        assertEquals(model, model);
        assertEquals(model, other);
        assertEquals(other, model);

        assertNotEquals(model, new Object());
    }

    @DisplayName("It should provide correct origin")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectOrigin(TypeVariableModel model,
            String name) {
        assertEquals(ctx.getReflectionOrigin(name), model.get());
    }

    static final class Context extends AbstractContext<AnnotatedTypeVariable> {
        private static final Map<String, TypeVariable<?>> parameters = Arrays
                .stream(Sample.class.getTypeParameters()).collect(Collectors
                        .toMap(TypeVariable::getName, Function.identity()));
        private static final Map<String, AnnotatedTypeVariable> reflectionOrigins = getDeclaredFields(
                Sample.class).map(Field::getAnnotatedType)
                .map(AnnotatedTypeVariable.class::cast)
                .collect(Collectors.toMap(
                        variable -> ((TypeVariable<?>) variable.getType())
                                .getName(),
                        Function.identity()));

        Context() {
            super(reflectionOrigins);
        }

        public TypeVariable<?> getParameter(String name) {
            return parameters.get(name);
        }
    }

    static final class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{1}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context)
                throws Exception {
            var ctx = new Context();

            return ctx.getReflectionOrigins().entrySet().stream()
                    .map(entry -> Arguments.of(
                            TypeVariableModel.of(entry.getValue()),
                            entry.getKey()));
        }

        static final class Checker
                extends SpecializationChecker<SpecializedModel> {
            Checker() {
                super(SpecializedModel.class,
                        getDeclaredMethods(SpecializedModel.class));
            }
        }

        static final class Equality implements ArgumentsProvider {
            public static final String testNamePattern = "{2}";

            @Override
            public Stream<Arguments> provideArguments(ExtensionContext context)
                    throws Exception {
                var ctx = new Context();

                return ctx.getReflectionOrigins().entrySet().stream()
                        .map(entry -> Arguments.of(
                                TypeVariableModel.of(entry.getValue()),
                                TypeVariableModel.of(entry.getValue()),
                                entry.getKey()));
            }
        }
    }

    @Nested
    @DisplayName("As a SpecializedModel")
    public class AsSpecializedModel {
        private final ModelProvider.Checker checker = new ModelProvider.Checker();

        @DisplayName("It should have a type argument specialization")
        @ParameterizedTest(name = ModelProvider.testNamePattern)
        @ArgumentsSource(ModelProvider.class)
        public void should_HaveSpecialization(TypeVariableModel model,
                String name) {
            checker.apply(model, "isTypeVariable", "isNonJDKClass");
        }
    }

    static class Sample<RegularTypeVariable, BoundedTypeVariable extends Sample.Bound> {
        @Foo
        private BoundedTypeVariable boundedTypeVariableField;
        @Foo
        private RegularTypeVariable regularTypeVariableField;

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE_USE)
        @interface Foo {
        }

        static class Bound {
        }
    }
}
