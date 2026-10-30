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
import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethod;
import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethods;
import static com.vaadin.hilla.parser.test.helpers.SpecializationChecker.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
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
import com.vaadin.hilla.parser.test.helpers.context.AbstractCharacteristics;
import com.vaadin.hilla.parser.test.helpers.context.AbstractContext;

public class MethodInfoModelTests {
    private Context.Default ctx;

    @BeforeEach
    public void setUp() {
        ctx = new Context.Default();
    }

    @DisplayName("It should have the same hashCode for models of the same origin")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveSameHashCodeForModelsOfSameOrigin(
            MethodInfoModel model, MethodInfoModel other, String name) {
        assertEquals(model.hashCode(), other.hashCode());
    }

    @DisplayName("It should have models of the same origin equal")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveModelsOfSameOriginEqual(MethodInfoModel model,
            MethodInfoModel other, String name) {
        assertEquals(model, model);
        assertEquals(model, other);
        assertEquals(other, model);

        assertNotEquals(model, new Object());
    }

    @DisplayName("It should provide correct origin")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectOrigin(MethodInfoModel model,
            String name) {
        assertEquals(ctx.getReflectionOrigin(name), model.get());
    }

    @DisplayName("It should provide correct type parameters")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectTypeParameters(MethodInfoModel model,
            String name) {
        switch (name) {
        case Context.Default.basicMethodName:
            assertEquals(List.of(), model.getTypeParameters());
            break;
        case Context.Default.genericMethodName:
            assertEquals(ctx.getTypeParameterOrigins().stream()
                    .map(TypeParameterModel::of).collect(Collectors.toList()),
                    model.getTypeParameters());
            break;
        }
    }

    @DisplayName("As a method model with characteristics")
    @Nested
    public class AsCharacterizedMethodModel {
        private final ModelProvider.Characteristics.Checker checker = new ModelProvider.Characteristics.Checker();

        @DisplayName("It should detect method characteristics correctly")
        @ParameterizedTest(name = ModelProvider.Characteristics.testNamePattern)
        @ArgumentsSource(ModelProvider.Characteristics.class)
        public void should_DetectCharacteristics(MethodInfoModel model,
                String[] characteristics, String testName) {
            checker.apply(model, characteristics);
        }
    }

    static abstract class Context {
        static final class Characteristics
                extends AbstractCharacteristics<Executable> {
            private static final Map<Executable, String[]> reflectionCharacteristics;

            static {
                var refClass = Sample.Characteristics.class;

                var bridgeMethod = getDeclaredMethod(
                        Sample.Characteristics.Bridge.class, "compareTo",
                        List.of(Object.class));

                reflectionCharacteristics = Map.ofEntries(
                        entry(getDeclaredConstructor(refClass), "isConstructor",
                                "isPublic"),
                        entry(getDeclaredMethod(refClass, "abstractMethod"),
                                "isPublic", "isAbstract"),
                        entry(getDeclaredMethod(refClass, "finalMethod"),
                                "isPublic", "isFinal"),
                        entry(getDeclaredMethod(refClass, "nativeMethod"),
                                "isPublic", "isNative"),
                        entry(getDeclaredMethod(refClass, "privateMethod"),
                                "isPrivate"),
                        entry(getDeclaredMethod(refClass, "protectedMethod"),
                                "isProtected"),
                        entry(getDeclaredMethod(refClass, "staticMethod"),
                                "isPublic", "isStatic"),
                        entry(getDeclaredMethod(refClass, "synchronizedMethod"),
                                "isPublic", "isSynchronized"),
                        entry(getDeclaredMethod(refClass, "varArgsMethod",
                                List.of(String[].class)), "isPublic",
                                "isVarArgs"),
                        entry(bridgeMethod, "isPublic", "isBridge",
                                "isSynthetic"));
            }

            Characteristics() {
                super(reflectionCharacteristics);
            }
        }

        static final class Default extends AbstractContext<Method> {
            public static final String basicMethodName = "method";
            public static final String genericMethodName = "genericMethod";
            private static final Map<String, Method> reflectionOrigins = getDeclaredMethods(
                    Sample.class)
                    .collect(Collectors.toMap(Method::getName,
                            Function.identity()));

            private static final List<TypeVariable<?>> typeParameterOrigins;

            static {
                typeParameterOrigins = Arrays.asList(reflectionOrigins
                        .get(genericMethodName).getTypeParameters());
            }

            Default() {
                super(reflectionOrigins);
            }

            public List<TypeVariable<?>> getTypeParameterOrigins() {
                return typeParameterOrigins;
            }
        }
    }

    static class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{1}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context.Default();

            return ctx.getReflectionOrigins().entrySet().stream()
                    .map(entry -> Arguments.of(
                            MethodInfoModel.of(entry.getValue()),
                            entry.getKey()));
        }

        static class Characteristics implements ArgumentsProvider {
            public static final String testNamePattern = "{2}";

            @Override
            public Stream<Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context.Characteristics();

                return ctx.getReflectionCharacteristics().entrySet().stream()
                        .map(entry -> Arguments.of(
                                MethodInfoModel.of(entry.getKey()),
                                entry.getValue(), entry.getKey().getName()));
            }

            static final class Checker
                    extends SpecializationChecker<MethodInfoModel> {
                private static final List<String> allowedMethods = List.of(
                        "isAbstract", "isBridge", "isConstructor", "isFinal",
                        "isNative", "isPrivate", "isProtected", "isPublic",
                        "isStatic", "isStrict", "isSynchronized", "isSynthetic",
                        "isVarArgs");

                public Checker() {
                    super(MethodInfoModel.class,
                            getDeclaredMethods(MethodInfoModel.class),
                            allowedMethods);
                }
            }
        }

        static class Equality implements ArgumentsProvider {
            public static final String testNamePattern = "{2}";

            @Override
            public Stream<Arguments> provideArguments(ExtensionContext context)
                    throws Exception {
                var ctx = new Context.Default();

                return ctx.getReflectionOrigins().entrySet().stream()
                        .map(entry -> Arguments.of(
                                MethodInfoModel.of(entry.getValue()),
                                MethodInfoModel.of(entry.getValue()),
                                entry.getKey()));
            }
        }
    }

    static class Sample {
        public <T> T genericMethod(T parameter) {
            return parameter;
        }

        public Dependency method(String first, ParamDependency second) {
            return null;
        }

        static abstract class Characteristics {
            public Characteristics() {
            }

            public static String staticMethod() {
                return "";
            }

            public final String finalMethod() {
                return "";
            }

            public abstract String abstractMethod();

            public native boolean nativeMethod();

            public synchronized String synchronizedMethod() {
                return "";
            }

            public String varArgsMethod(String... input) {
                return "";
            }

            protected String protectedMethod() {
                return "";
            }

            private String privateMethod() {
                return "";
            }

            static class Bridge implements Comparable<Bridge> {
                @Override
                public int compareTo(Bridge o) {
                    return 0;
                }
            }
        }

        static class Dependency {
        }

        static class ParamDependency {
        }
    }
}
