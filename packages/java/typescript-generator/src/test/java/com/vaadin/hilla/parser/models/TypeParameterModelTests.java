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

import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.vaadin.hilla.parser.test.helpers.SpecializationChecker;
import com.vaadin.hilla.parser.test.helpers.context.AbstractContext;

public class TypeParameterModelTests {
    private Context ctx;

    @BeforeEach
    public void setUp() {
        this.ctx = new Context();
    }

    @DisplayName("It should get annotations")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetAnnotations(TypeParameterModel model, String name) {
        assertEquals(List.of(Sample.Foo.class.getName()),
                model.getAnnotations().stream()
                        .map(AnnotationInfoModel::getName)
                        .collect(Collectors.toList()));
    }

    @DisplayName("It should get bounds")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetBounds(TypeParameterModel model, String name) {
        switch (name) {
        case "RegularTypeParameter":
            assertEquals(List.of(Object.class.getName()),
                    model.getBounds().stream().map(NamedModel.class::cast)
                            .map(NamedModel::getName)
                            .collect(Collectors.toList()));
            break;
        case "BoundedTypeParameter":
            assertEquals(List.of(Sample.Bound.class.getName()),
                    model.getBounds().stream().map(NamedModel.class::cast)
                            .map(NamedModel::getName)
                            .collect(Collectors.toList()));
            break;
        }
    }

    @DisplayName("It should compare an F-bounded type parameter")
    @Test
    public void should_CompareFBoundedTypeParameter() {
        var origin = FBoundedSample.class.getTypeParameters()[0];
        var otherOrigin = OtherFBoundedSample.class.getTypeParameters()[0];

        assertEquals(TypeParameterModel.of(origin),
                TypeParameterModel.of(origin));

        // Both are named E and both are F-bounded, so only the bounds tell
        // them apart: breaking out of the cycle may not stop the comparison
        // before they are reached.
        assertNotEquals(TypeParameterModel.of(origin),
                TypeParameterModel.of(otherOrigin));
        assertNotEquals(TypeParameterModel.of(otherOrigin),
                TypeParameterModel.of(origin));
    }

    @DisplayName("It should compare mutually bounded type parameters")
    @Test
    public void should_CompareMutuallyBoundedTypeParameters() {
        var origin = MutuallyBoundedSample.class.getTypeParameters()[0];
        var otherOrigin = OtherMutuallyBoundedSample.class
                .getTypeParameters()[0];

        assertEquals(TypeParameterModel.of(origin),
                TypeParameterModel.of(origin));

        // Both are named L and both are bounded by an R, so only the bound of
        // R, reached on the way round the cycle, tells them apart.
        assertNotEquals(TypeParameterModel.of(origin),
                TypeParameterModel.of(otherOrigin));
        assertNotEquals(TypeParameterModel.of(otherOrigin),
                TypeParameterModel.of(origin));
    }

    @DisplayName("It should have the same hashCode for models of the same origin")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveSameHashCodeForModelsOfSameOrigin(
            TypeParameterModel model, TypeParameterModel other, String name) {
        assertEquals(model.hashCode(), other.hashCode());
    }

    @DisplayName("It should have models of the same origin equal")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveModelsOfSameOriginEqual(TypeParameterModel model,
            TypeParameterModel other, String name) {
        assertEquals(model, model);
        assertEquals(model, other);
        assertEquals(other, model);

        assertNotEquals(model, new Object());
    }

    @DisplayName("It should provide correct origin")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectOrigin(TypeParameterModel model,
            String name) {
        assertEquals(ctx.getReflectionOrigin(name), model.get());
    }

    static final class Context extends AbstractContext<TypeVariable<?>> {
        private static final Map<String, TypeVariable<?>> reflectionOrigins = Arrays
                .stream(Sample.class.getTypeParameters()).collect(Collectors
                        .toMap(TypeVariable::getName, Function.identity()));

        Context() {
            super(reflectionOrigins);
        }
    }

    static final class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{1}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context();

            return ctx.getReflectionOrigins().entrySet().stream()
                    .map(entry -> Arguments.of(
                            TypeParameterModel.of(entry.getValue()),
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
            public Stream<Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context();

                return ctx.getReflectionOrigins().entrySet().stream()
                        .map(entry -> Arguments.of(
                                TypeParameterModel.of(entry.getValue()),
                                TypeParameterModel.of(entry.getValue()),
                                entry.getKey()));
            }
        }
    }

    /**
     * An F-bounded type parameter: its bound leads back to the type parameter
     * itself, which is the shape of the type parameter of {@link Enum} and of
     * any type parameterized by an enum.
     */
    static final class FBoundedSample<E extends Enum<E>> {
    }

    /**
     * Another F-bounded type parameter, of the same name as the one of
     * {@link FBoundedSample} but with a different bound.
     */
    static final class OtherFBoundedSample<E extends Comparable<E>> {
    }

    /**
     * Type parameters bounded by each other: comparing the bound of either
     * compares the other one on the way back to it.
     */
    static final class MutuallyBoundedSample<L extends Comparable<R>, R extends Comparable<L>> {
    }

    /**
     * Another sample starting the same way as {@link MutuallyBoundedSample},
     * but whose {@code R} is not bounded by {@code L}.
     */
    static final class OtherMutuallyBoundedSample<L extends Comparable<R>, R extends Number> {
    }

    static final class Sample<@Sample.Foo RegularTypeParameter, @Sample.Foo BoundedTypeParameter extends Sample.Bound> {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE_USE)
        @interface Foo {
        }

        static class Bound {
        }
    }

    @Nested
    @DisplayName("As a SpecializedModel")
    public class AsSpecializedModel {
        private final ModelProvider.Checker checker = new ModelProvider.Checker();

        @DisplayName("It should have a type argument specialization")
        @ParameterizedTest(name = ModelProvider.testNamePattern)
        @ArgumentsSource(ModelProvider.class)
        public void should_HaveSpecialization(TypeParameterModel model,
                String name) {
            checker.apply(model, "isTypeParameter", "isNonJDKClass");
        }
    }
}
