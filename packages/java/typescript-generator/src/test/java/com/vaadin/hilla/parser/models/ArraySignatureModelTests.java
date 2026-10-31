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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedType;
import java.util.List;
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

public class ArraySignatureModelTests {
    private Context ctx;

    @BeforeEach
    public void setUp() {
        ctx = new Context();
    }

    @DisplayName("It should have the same hashCode for models of the same origin")
    @Test
    public void should_HaveSameHashCodeForModelsOfSameOrigin() {
        var model = ArraySignatureModel.of(ctx.getReflectionOrigin());
        var otherModel = ArraySignatureModel.of(ctx.getReflectionOrigin());

        assertEquals(model.hashCode(), otherModel.hashCode());
    }

    @DisplayName("It should have models of the same origin equal")
    @Test
    public void should_HaveModelsOfSameOriginEqual() {
        var model = ArraySignatureModel.of(ctx.getReflectionOrigin());
        var otherModel = ArraySignatureModel.of(ctx.getReflectionOrigin());

        assertEquals(model, model);
        assertEquals(model, otherModel);
        assertEquals(otherModel, model);

        assertNotEquals(model, new Object());
    }

    @DisplayName("It should provide correct origin")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectOrigin(ArraySignatureModel model) {
        assertEquals(ctx.getReflectionOrigin(), model.get());
    }

    @DisplayName("It should provide nested type")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideNestedType(ArraySignatureModel model) {
        var nested = model.getNestedType();
        assertTrue(nested instanceof ClassRefSignatureModel);

        var nestedOrigin = (AnnotatedType) nested.get();
        assertEquals(nestedOrigin.getType().getTypeName(),
                Dependency.class.getName());
    }

    static final class Context {
        private static final String fieldName = "foo";
        private static final Annotation annotation = ((AnnotatedArrayType) getDeclaredField(
                Sample.class, fieldName).getAnnotatedType())
                .getAnnotatedGenericComponentType()
                .getAnnotation(Sample.Bar.class);
        private static final AnnotatedArrayType reflectionOrigin = (AnnotatedArrayType) getDeclaredField(
                Sample.class, fieldName).getAnnotatedType();

        public Annotation getAnnotation() {
            return annotation;
        }

        public AnnotatedArrayType getReflectionOrigin() {
            return reflectionOrigin;
        }

    }

    static final class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{0}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context();

            return Stream.of(Arguments
                    .of(ArraySignatureModel.of(ctx.getReflectionOrigin())));
        }

        static final class Checker
                extends SpecializationChecker<SpecializedModel> {
            Checker() {
                super(SpecializedModel.class,
                        getDeclaredMethods(SpecializedModel.class));
            }
        }
    }

    @Nested
    @DisplayName("As an AnnotatedModel")
    public class AsAnnotatedModel {
        @DisplayName("It should access annotations")
        @ParameterizedTest(name = ModelProvider.testNamePattern)
        @ArgumentsSource(ModelProvider.class)
        public void should_AccessAnnotations(ArraySignatureModel model) {
            assertEquals(List.of(AnnotationInfoModel.of(ctx.getAnnotation())),
                    model.getNestedType().getAnnotations());
        }
    }

    @Nested
    @DisplayName("As a SpecializedModel")
    public class AsSpecializedModel {
        private final ModelProvider.Checker checker = new ModelProvider.Checker();

        @DisplayName("It should have an array specialization")
        @ParameterizedTest(name = ModelProvider.testNamePattern)
        @ArgumentsSource(ModelProvider.class)
        public void should_HaveArraySpecialization(ArraySignatureModel model) {
            checker.apply(model, "isArray", "isNonJDKClass");
        }
    }

    static class Dependency {
    }

    static class Sample {
        @Bar
        private Dependency[] foo;

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE_USE)
        @interface Bar {
        }
    }
}
