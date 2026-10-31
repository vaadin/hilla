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
import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredFields;
import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethods;
import static com.vaadin.hilla.parser.test.helpers.SpecializationChecker.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
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

public class FieldInfoModelTests {
    private Context.Default ctx;

    @BeforeEach
    public void setUp() {
        ctx = new Context.Default();
    }

    @DisplayName("It should provide field name")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetName(FieldInfoModel model) {
        assertEquals("field", model.getName());
    }

    @DisplayName("It should get the field's type")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetType(FieldInfoModel model) {
        var expected = SignatureModel
                .of(ctx.getReflectionOrigin().getAnnotatedType());

        assertEquals(expected, model.getType());
    }

    @DisplayName("It should have the same hashCode for models of the same origin")
    @Test
    public void should_HaveSameHashCodeForModelsOfSameOrigin() {
        var model = FieldInfoModel.of(ctx.getReflectionOrigin());
        var other = FieldInfoModel.of(ctx.getReflectionOrigin());

        assertEquals(model.hashCode(), other.hashCode());
    }

    @DisplayName("It should have models of the same origin equal")
    @Test
    public void should_HaveModelsOfSameOriginEqual() {
        var model = FieldInfoModel.of(ctx.getReflectionOrigin());
        var other = FieldInfoModel.of(ctx.getReflectionOrigin());

        assertEquals(model, model);
        assertEquals(model, other);
        assertEquals(other, model);

        assertNotEquals(model, new Object());
    }

    @DisplayName("It should provide correct origin")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectOrigin(FieldInfoModel model) {
        assertEquals(ctx.getReflectionOrigin(), model.get());
    }

    public static final class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{0}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context.Default();

            return Stream.of(
                    Arguments.of(FieldInfoModel.of(ctx.getReflectionOrigin())));
        }

        public static final class Characteristics implements ArgumentsProvider {
            public static final String testNamePattern = "{2}";

            @Override
            public Stream<Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context.Characteristics();

                return ctx.getReflectionCharacteristics().entrySet().stream()
                        .map(entry -> Arguments.of(
                                FieldInfoModel.of(entry.getKey()),
                                entry.getValue(), entry.getKey().getName()));
            }

        }

        public static final class CharacterizedChecker
                extends SpecializationChecker<FieldInfoModel> {
            private static final List<String> allowedMethods = List.of("isEnum",
                    "isFinal", "isPrivate", "isProtected", "isPublic",
                    "isStatic", "isSynthetic", "isTransient");

            public CharacterizedChecker() {
                super(FieldInfoModel.class,
                        getDeclaredMethods(FieldInfoModel.class),
                        allowedMethods);
            }
        }
    }

    static final class Sample {
        @Foo
        private Dependency field;

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.FIELD)
        @interface Foo {
        }

        static final class Dependency {
        }
    }

    @Nested
    @DisplayName("As an AnnotatedModel")
    public class AsAnnotatedModel {
        @DisplayName("It should access annotation")
        @ParameterizedTest(name = ModelProvider.testNamePattern)
        @ArgumentsSource(ModelProvider.class)
        public void should_AccessAnnotation(FieldInfoModel model) {
            assertEquals(List.of(AnnotationInfoModel.of(ctx.getAnnotation())),
                    model.getAnnotations());
        }
    }

    @DisplayName("As a field model with characteristics")
    @Nested
    public class AsCharacterizedFieldModel {
        private final ModelProvider.CharacterizedChecker checker = new ModelProvider.CharacterizedChecker();

        @DisplayName("It should detect field characteristics correctly")
        @ParameterizedTest(name = ModelProvider.Characteristics.testNamePattern)
        @ArgumentsSource(ModelProvider.Characteristics.class)
        public void should_DetectCharacteristics(FieldInfoModel model,
                String[] characteristics, String testName) {
            checker.apply(model, characteristics);
        }
    }

    static class Characteristics {
        public static final String publicStaticFinalField = "";
        private static String staticField;
        private final String finalField = "";
        public String publicField;
        public transient String transientField;
        protected String protectedField;
        private String privateField;

        public enum Enum {
            ENUM_FIELD,
        }
    }

    static abstract class Context {
        static final class Characteristics
                extends AbstractCharacteristics<Field> {
            private static final Map<Field, String[]> reflectionAssociations;

            static {
                var refClass = FieldInfoModelTests.Characteristics.class;
                var refEnumClass = FieldInfoModelTests.Characteristics.Enum.class;
                reflectionAssociations = Map.ofEntries(
                        entry(getDeclaredField(refClass, "publicField"),
                                "isPublic"),
                        entry(getDeclaredField(refClass, "protectedField"),
                                "isProtected"),
                        entry(getDeclaredField(refClass, "privateField"),
                                "isPrivate"),
                        entry(getDeclaredField(refClass, "staticField"),
                                "isPrivate", "isStatic"),
                        entry(getDeclaredField(refClass, "finalField"),
                                "isFinal", "isPrivate"),
                        entry(getDeclaredField(refClass,
                                "publicStaticFinalField"), "isFinal",
                                "isPublic", "isStatic"),
                        entry(getDeclaredField(refClass, "transientField"),
                                "isPublic", "isTransient"),
                        entry(getDeclaredField(refEnumClass, "ENUM_FIELD"),
                                "isEnum", "isFinal", "isPublic", "isStatic"));
            }

            Characteristics() {
                super(reflectionAssociations);
            }
        }

        static final class Default extends AbstractContext<Field> {
            private static final Annotation annotation;
            private static final String defaultFieldName = "field";
            private static final Map<String, Field> reflectionOrigins = getDeclaredFields(
                    Sample.class)
                    .collect(Collectors.toMap(Field::getName,
                            Function.identity()));

            static {
                annotation = reflectionOrigins.get(defaultFieldName)
                        .getAnnotation(Sample.Foo.class);
            }

            Default() {
                super(reflectionOrigins);
            }

            public Annotation getAnnotation() {
                return annotation;
            }

            public Field getReflectionOrigin() {
                return getReflectionOrigin(defaultFieldName);
            }
        }
    }
}
