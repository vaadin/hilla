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

import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredField;
import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.function.Failable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class AnnotationInfoModelTests {
    private Context ctx;

    @BeforeEach
    public void setUp() {
        ctx = new Context();
    }

    @DisplayName("It should get class info")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetClassInfo(AnnotationInfoModel model) {
        assertEquals(Optional.of(ClassInfoModel.of(Sample.Foo.class)),
                model.getClassInfo());
    }

    @DisplayName("It should get annotations parameters")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetParameters(AnnotationInfoModel model) {
        var expected = Set.of(
                AnnotationParameterModel.of("stringParameter", "foo1", false),
                AnnotationParameterModel.of("intParameter", 10, false),
                AnnotationParameterModel.of("classParameter", Sample.class,
                        false),
                AnnotationParameterModel.of("enumParameter", Sample.Enum.VALUE,
                        false),
                AnnotationParameterModel.of("classArrayParameter",
                        new Class<?>[] { Sample.class, Sample.Enum.class },
                        false),
                AnnotationParameterModel.of("enumArrayParameter",
                        new Sample.Enum[] { Sample.Enum.VALUE,
                                Sample.Enum.OTHER_VALUE },
                        false),
                AnnotationParameterModel.of("nestedParameter",
                        ctx.getReflectionOrigin().nestedParameter(), false));
        var actual = model.getParameters();
        assertEquals(expected, actual);
    }

    @DisplayName("It should unwrap array and nested annotation parameters")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_UnwrapCompoundParameters(AnnotationInfoModel model) {
        var parameters = model.getParameters().stream().collect(Collectors
                .toMap(AnnotationParameterModel::getName, Function.identity()));

        assertEquals(
                List.of(ClassInfoModel.of(Sample.class),
                        ClassInfoModel.of(Sample.Enum.class)),
                parameters.get("classArrayParameter").getValue());
        assertEquals(
                List.of(AnnotationParameterEnumValueModel.of(Sample.Enum.VALUE),
                        AnnotationParameterEnumValueModel
                                .of(Sample.Enum.OTHER_VALUE)),
                parameters.get("enumArrayParameter").getValue());

        var nested = (AnnotationInfoModel) parameters.get("nestedParameter")
                .getValue();
        assertEquals(Sample.Bar.class.getName(), nested.getName());
        assertEquals(
                Set.of(AnnotationParameterModel.of("intParameter", 20, false)),
                nested.getParameters());
    }

    @DisplayName("It should have the same hashCode for models of the same origin")
    @Test
    public void should_HaveSameHashCodeForModelsOfSameOrigin() {
        var model = AnnotationInfoModel.of(ctx.getReflectionOrigin());
        var other = AnnotationInfoModel.of(ctx.getReflectionOrigin());

        assertEquals(model.hashCode(), other.hashCode());
    }

    @DisplayName("It should have models of the same origin equal")
    @Test
    public void should_HaveModelsOfSameOriginEqual() {
        var model = AnnotationInfoModel.of(ctx.getReflectionOrigin());
        var other = AnnotationInfoModel.of(ctx.getReflectionOrigin());

        assertEquals(model, model);
        assertEquals(model, other);
        assertEquals(other, model);

        assertNotEquals(model, new Object());
    }

    @DisplayName("It should provide correct origin")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectOrigin(AnnotationInfoModel model) {
        assertEquals(ctx.getReflectionOrigin(), model.get());
    }

    static final class Context {
        private static final String fieldName = "bar";
        private static final Enum<?> reflectionEnumValue;
        private static final Sample.Foo reflectionOrigin = getDeclaredField(
                Sample.class, fieldName).getAnnotation(Sample.Foo.class);
        private static final Map<String, AnnotationParameterModel.ReflectionOrigin<?>> reflectionParameterOrigins;

        static {
            reflectionParameterOrigins = getDeclaredMethods(Sample.Foo.class)
                    .collect(Collectors.toMap(Method::getName,
                            Failable.asFunction(
                                    method -> new AnnotationParameterModel.ReflectionOrigin<>(
                                            method.getName(),
                                            method.invoke(reflectionOrigin),
                                            method.isDefault()))));
            reflectionEnumValue = (Enum<?>) reflectionParameterOrigins
                    .get("enumParameter").getValue();
        }

        public Enum<?> getReflectionEnumValue() {
            return reflectionEnumValue;
        }

        public Sample.Foo getReflectionOrigin() {
            return reflectionOrigin;
        }

        public Map<String, AnnotationParameterModel.ReflectionOrigin<?>> getReflectionParameterOrigins() {
            return reflectionParameterOrigins;
        }
    }

    static final class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{0}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context();

            return Stream.of(Arguments
                    .of(AnnotationInfoModel.of(ctx.getReflectionOrigin())));
        }
    }

    static final class Sample {
        @Foo(stringParameter = "foo1", classParameter = Sample.class, enumParameter = Enum.VALUE, classArrayParameter = {
                Sample.class, Enum.class }, enumArrayParameter = { Enum.VALUE,
                        Enum.OTHER_VALUE }, nestedParameter = @Bar(intParameter = 20))
        private String bar;

        enum Enum {
            VALUE, OTHER_VALUE
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.FIELD)
        @interface Foo {
            Class<?>[] classArrayParameter();

            Class<?> classParameter();

            Enum[] enumArrayParameter();

            Enum enumParameter();

            int intParameter() default 10;

            Bar nestedParameter();

            String stringParameter() default "bar1";
        }

        @Retention(RetentionPolicy.RUNTIME)
        @interface Bar {
            int intParameter() default 10;
        }
    }

    @Nested
    @DisplayName("As a NamedModel")
    public class AsNamedModel {
        @DisplayName("It should have name")
        @ParameterizedTest(name = ModelProvider.testNamePattern)
        @ArgumentsSource(ModelProvider.class)
        public void should_HaveName(AnnotationInfoModel model) {
            assertEquals(Sample.Foo.class.getName(), model.getName());
        }
    }

    @DisplayName("Annotation enum value parameter")
    @Nested
    public class EnumValue {
        @DisplayName("It should provide all information about value")
        @ParameterizedTest(name = EnumValueProvider.testNamePattern)
        @ArgumentsSource(EnumValueProvider.class)
        public void should_GetClassInfo(
                AnnotationParameterEnumValueModel model) {
            assertEquals(ClassInfoModel.of(Sample.Enum.class),
                    model.getClassInfo());
            assertEquals("VALUE", model.getValueName());
        }

        @DisplayName("It should have the same hashCode for models of the same origin")
        @Test
        public void should_HaveSameHashCodeForModelsOfSameOrigin() {
            var model = AnnotationParameterEnumValueModel
                    .of(ctx.getReflectionEnumValue());
            var other = AnnotationParameterEnumValueModel
                    .of(ctx.getReflectionEnumValue());

            assertEquals(model.hashCode(), other.hashCode());
        }

        @DisplayName("It should have models of the same origin equal")
        @Test
        public void should_HaveModelsOfSameOriginEqual() {
            var model = AnnotationParameterEnumValueModel
                    .of(ctx.getReflectionEnumValue());
            var other = AnnotationParameterEnumValueModel
                    .of(ctx.getReflectionEnumValue());

            assertEquals(model, model);
            assertEquals(model, other);
            assertEquals(other, model);

            assertNotEquals(model, new Object());
        }
    }

    @DisplayName("Annotation parameter")
    @Nested
    public class Parameter {
        @DisplayName("It should get name and value correctly")
        @ParameterizedTest(name = ParameterProvider.testNamePattern)
        @ArgumentsSource(ParameterProvider.class)
        public void should_GetNameAndValue(AnnotationParameterModel model,
                String name) {
            assertEquals(name, model.getName());
            assertEquals(process(
                    ctx.getReflectionParameterOrigins().get(name).getValue()),
                    model.getValue());
        }

        @DisplayName("It should have the same hashCode for models of the same origin")
        @ParameterizedTest(name = EqualityParameterProvider.testNamePattern)
        @ArgumentsSource(EqualityParameterProvider.class)
        public void should_HaveSameHashCodeForModelsOfSameOrigin(
                AnnotationParameterModel model, AnnotationParameterModel other,
                String name) {
            assertEquals(model.hashCode(), other.hashCode());
        }

        @DisplayName("It should have models of the same origin equal")
        @ParameterizedTest(name = EqualityParameterProvider.testNamePattern)
        @ArgumentsSource(EqualityParameterProvider.class)
        public void should_HaveModelsOfSameOriginEqual(
                AnnotationParameterModel model, AnnotationParameterModel other,
                String name) {
            assertEquals(model, model);
            assertEquals(model, other);
            assertEquals(other, model);

            assertNotEquals(model, new Object());
        }

        private Object process(Object value) {
            if (value instanceof Class<?>) {
                return ClassInfoModel.of((Class<?>) value);
            } else if (value instanceof Enum<?>) {
                return AnnotationParameterEnumValueModel.of((Enum<?>) value);
            } else if (value instanceof Annotation) {
                return AnnotationInfoModel.of((Annotation) value);
            } else if (value instanceof Object[]) {
                return Arrays.stream((Object[]) value).map(this::process)
                        .toList();
            } else {
                return value;
            }
        }
    }

    static class EnumValueProvider implements ArgumentsProvider {
        private static final String testNamePattern = "{0}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context();

            return Stream.of(Arguments.of(AnnotationParameterEnumValueModel
                    .of(ctx.getReflectionEnumValue())));
        }
    }

    static class EqualityParameterProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{2}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context();

            return ctx.getReflectionParameterOrigins().values().stream()
                    .map(origin -> Arguments.of(
                            AnnotationParameterModel.of(origin),
                            AnnotationParameterModel.of(origin),
                            origin.getName()));
        }
    }

    static class ParameterProvider implements ArgumentsProvider {
        private static final String testNamePattern = "{1}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context();

            return ctx.getReflectionParameterOrigins().values().stream()
                    .map(origin -> Arguments.of(
                            AnnotationParameterModel.of(origin),
                            origin.getName()));
        }
    }
}
