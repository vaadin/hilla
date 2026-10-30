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

import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredConstructor;
import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethods;
import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getParameter;
import static com.vaadin.hilla.parser.test.helpers.SpecializationChecker.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
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
import com.vaadin.hilla.parser.test.helpers.context.AbstractCharacteristics;
import com.vaadin.hilla.parser.test.helpers.context.AbstractContext;

public class MethodParameterInfoModelTests {
    private static final String defaultParameterName = "finalParameter";
    private Context.Default ctx;

    @BeforeEach
    public void setUp() {
        ctx = new Context.Default();
    }

    @DisplayName("It should have the same hashCode for models of the same origin")
    @Test
    public void should_HaveSameHashCodeForModelsOfSameOrigin() {
        var model = MethodParameterInfoModel
                .of(ctx.getReflectionOrigin(defaultParameterName));
        var other = MethodParameterInfoModel
                .of(ctx.getReflectionOrigin(defaultParameterName));

        assertEquals(model.hashCode(), other.hashCode());
    }

    @DisplayName("It should have models of the same origin equal")
    @Test
    public void should_HaveModelsOfSameOriginEqual() {
        var model = MethodParameterInfoModel
                .of(ctx.getReflectionOrigin(defaultParameterName));
        var other = MethodParameterInfoModel
                .of(ctx.getReflectionOrigin(defaultParameterName));

        assertEquals(model, model);
        assertEquals(model, other);
        assertEquals(other, model);

        assertNotEquals(model, new Object());
    }

    @DisplayName("It should index parameters correctly")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_IndexParameters(MethodParameterInfoModel model) {
        assertEquals(1, model.getIndex());
    }

    @DisplayName("It should provide correct origin")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectOrigin(MethodParameterInfoModel model) {
        assertEquals(ctx.getReflectionOrigin(defaultParameterName),
                model.get());
    }

    static final class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{0}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context.Default();

            return Stream.of(Arguments.of(MethodParameterInfoModel
                    .of(ctx.getReflectionOrigin(defaultParameterName))));
        }

        static final class Characteristics implements ArgumentsProvider {
            public static final String testNamePattern = "{2}";

            @Override
            public Stream<Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context.Characteristics();

                return ctx.getReflectionCharacteristics().entrySet().stream()
                        .map(entry -> Arguments.of(
                                MethodParameterInfoModel.of(entry.getKey()),
                                entry.getValue(), entry.getKey().getName()));
            }

            public static final class Checker
                    extends SpecializationChecker<MethodParameterInfoModel> {
                private static final List<String> allowedMethods = List.of(
                        "isFinal", "isMandated", "isImplicit", "isSynthetic");

                public Checker() {
                    super(MethodParameterInfoModel.class,
                            getDeclaredMethods(MethodParameterInfoModel.class),
                            allowedMethods);
                }
            }
        }
    }

    @DisplayName("As a method parameter model with characteristics")
    @Nested
    public class AsCharacterizedMethodModel {
        private final ModelProvider.Characteristics.Checker checker = new ModelProvider.Characteristics.Checker();

        @DisplayName("It should detect method characteristics correctly")
        @ParameterizedTest(name = ModelProvider.Characteristics.testNamePattern)
        @ArgumentsSource(ModelProvider.Characteristics.class)
        public void should_DetectCharacteristics(MethodParameterInfoModel model,
                String[] characteristics, String testName) {
            checker.apply(model, characteristics);
        }
    }

    static class Context {
        static class Characteristics
                extends AbstractCharacteristics<Parameter> {
            private static final Map<Parameter, String[]> reflectionCharacteristics;

            static {
                /*
                 * class Sample { class Dyn { Dyn(final String finalParameter,
                 * int defaultParameter) { } } }
                 *
                 * has the following constructor generated by the compiler:
                 *
                 * class Sample { class Dyn { Dyn(Sample this$0, final String
                 * finalParameter, int defaultParameter) { } } }
                 */
                var dynConstructor = getDeclaredConstructor(Sample.Dyn.class,
                        List.of(Sample.class, String.class, int.class));

                /*
                 * enum Enum { FOO, BAR }
                 *
                 * has the following constructor generated by the compiler:
                 *
                 * final class Enum extends java.lang.Enum<Colors> { ...
                 *
                 * private Enum(String $enum$name, int $enum$ordinal) {
                 * super(name, ordinal); }
                 *
                 * ... }
                 */
                var enumConstructor = getDeclaredConstructor(Sample.Enum.class,
                        List.of(String.class, int.class));

                reflectionCharacteristics = Map.ofEntries(
                        entry(getParameter(dynConstructor, 0), "isMandated",
                                "isImplicit", "isFinal"),
                        entry(getParameter(dynConstructor, 1), "isFinal"),
                        entry(getParameter(dynConstructor, 2)),
                        entry(getParameter(enumConstructor, 0), "isSynthetic"),
                        entry(getParameter(enumConstructor, 1), "isSynthetic"));
            }

            Characteristics() {
                super(reflectionCharacteristics);
            }
        }

        static class Default extends AbstractContext<Parameter> {
            private static final List<Class<?>> constructorParams = List
                    .of(Sample.class, String.class, int.class);

            private static final Map<String, Parameter> reflectionOrigins = Arrays
                    .stream(getDeclaredConstructor(Sample.Dyn.class,
                            constructorParams).getParameters())
                    .collect(Collectors.toMap(Parameter::getName,
                            Function.identity()));

            Default() {
                super(reflectionOrigins);
            }
        }
    }

    static class Sample {
        enum Enum {
            FOO, BAR
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE_USE)
        @interface Foo {
        }

        class Dyn {
            Dyn(@Foo final String finalParameter, @Foo int regularParameter) {
            }
        }
    }
}
