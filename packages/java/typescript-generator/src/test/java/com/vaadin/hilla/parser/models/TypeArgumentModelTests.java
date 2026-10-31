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
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
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

public class TypeArgumentModelTests {
    private static final String defaultFieldName = "regularTypeArgument";
    private Context ctx;

    @BeforeEach
    public void setUp() {
        this.ctx = new Context();
    }

    @DisplayName("It should get annotations")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetAnnotations(TypeArgumentModel model, String name) {
        assertEquals(List.of(Sample.Foo.class.getName()),
                model.getAnnotations().stream()
                        .map(AnnotationInfoModel::getName)
                        .collect(Collectors.toList()));
    }

    @DisplayName("It should have correct widlcard")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_HaveCorrectWildcard(TypeArgumentModel model,
            String name) {
        switch (name) {
        case "anyTypeArgument":
            assertEquals(TypeArgumentModel.Wildcard.ANY, model.getWildcard());
            break;
        case "extendsTypeArgument":
            assertEquals(TypeArgumentModel.Wildcard.EXTENDS,
                    model.getWildcard());
            break;
        case "regularTypeArgument":
            assertEquals(TypeArgumentModel.Wildcard.NONE, model.getWildcard());
            break;
        case "superTypeArgument":
            assertEquals(TypeArgumentModel.Wildcard.SUPER, model.getWildcard());
            break;
        }
    }

    @DisplayName("It should have the same hashCode for models of the same origin")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveSameHashCodeForModelsOfSameOrigin(
            TypeArgumentModel model, TypeArgumentModel other, String name) {
        assertEquals(model.hashCode(), other.hashCode());
    }

    @DisplayName("It should have models of the same origin equal")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveModelsOfSameOriginEqual(TypeArgumentModel model,
            TypeArgumentModel other, String name) {
        assertEquals(model, model);
        assertEquals(model, other);
        assertEquals(other, model);

        assertNotEquals(model, new Object());
    }

    @DisplayName("It should provide correct origin")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectOrigin(TypeArgumentModel model,
            String name) {
        assertEquals(ctx.getReflectionOrigin(name), model.get());
    }

    static final class Context extends AbstractContext<AnnotatedType> {
        private static final Map<String, AnnotatedType> reflectionOrigins = getDeclaredFields(
                Sample.class)
                .collect(Collectors.toMap(Field::getName,
                        field -> ((AnnotatedParameterizedType) field
                                .getAnnotatedType())
                                .getAnnotatedActualTypeArguments()[0]));

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
                            TypeArgumentModel.of(entry.getValue()),
                            entry.getKey()));
        }

        static final class Equality implements ArgumentsProvider {
            private static final String testNamePattern = "{2}";

            @Override
            public Stream<Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context();

                return ctx.getReflectionOrigins().entrySet().stream()
                        .map(entry -> Arguments.of(
                                TypeArgumentModel.of(entry.getValue()),
                                TypeArgumentModel.of(entry.getValue()),
                                entry.getKey()));
            }
        }

        static class Checker extends SpecializationChecker<SpecializedModel> {
            Checker() {
                super(SpecializedModel.class,
                        getDeclaredMethods(SpecializedModel.class));
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
        public void should_HaveSpecialization(TypeArgumentModel model,
                String name) {
            checker.apply(model, "isTypeArgument", "isNonJDKClass");
        }
    }

    static class Sample {
        List<@Foo ?> anyTypeArgument;
        List<@Foo ? extends Association> extendsTypeArgument;
        List<@Foo Association> regularTypeArgument;
        List<@Foo ? super Association> superTypeArgument;

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE_USE)
        @interface Foo {
        }

        static class Association {
        }
    }
}
