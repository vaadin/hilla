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

import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.cleanup;
import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredFields;
import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethods;
import static com.vaadin.hilla.parser.test.helpers.SpecializationChecker.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
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

import com.vaadin.hilla.parser.test.helpers.ClassMemberUtils;
import com.vaadin.hilla.parser.test.helpers.SpecializationChecker;

public class ClassInfoModelTests {
    private Context.Default ctx;

    @BeforeEach
    public void setUp() {
        ctx = new Context.Default();
    }

    @DisplayName("It should check assignability from other classes")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_CheckAssignability(ClassInfoModel model) {
        var assignableReflectionClass = Dependency.SampleChild.class;
        var nonAssignableReflectionClass = Dependency.Sample.StaticInner.class;

        assertTrue(model.isAssignableFrom(assignableReflectionClass));
        assertTrue(model.isAssignableFrom(
                ClassInfoModel.of(assignableReflectionClass)));

        assertFalse(model.isAssignableFrom(nonAssignableReflectionClass));
        assertFalse(model.isAssignableFrom(
                ClassInfoModel.of(nonAssignableReflectionClass)));

        assertTrue(ClassInfoModel.isAssignableFrom(model.getName(),
                assignableReflectionClass));
        assertTrue(ClassInfoModel.isAssignableFrom(model.getName(),
                ClassInfoModel.of(assignableReflectionClass)));

        assertFalse(ClassInfoModel.isAssignableFrom(model.getName(),
                nonAssignableReflectionClass));
        assertFalse(ClassInfoModel.isAssignableFrom(model.getName(),
                ClassInfoModel.of(nonAssignableReflectionClass)));

        var refOrigin = ctx.getReflectionOrigin();
        assertTrue(ClassInfoModel.isAssignableFrom(refOrigin,
                assignableReflectionClass));
        assertTrue(ClassInfoModel.isAssignableFrom(refOrigin,
                ClassInfoModel.of(assignableReflectionClass)));

        assertFalse(ClassInfoModel.isAssignableFrom(refOrigin,
                nonAssignableReflectionClass));
        assertFalse(ClassInfoModel.isAssignableFrom(refOrigin,
                ClassInfoModel.of(nonAssignableReflectionClass)));
    }

    @DisplayName("It should check equality")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_CheckEquality(ClassInfoModel model) {
        var reflectionModel = ClassInfoModel.of(ctx.getReflectionOrigin());

        var otherReflectionModel = ClassInfoModel
                .of(Dependency.SampleChild.class);

        assertEquals(model, model);
        assertEquals(model, reflectionModel);
        assertNotEquals(model, otherReflectionModel);
        assertNotEquals(model, mock(MethodInfoModel.class));
    }

    @DisplayName("It should check if the class belongs to JDK")
    @Test
    public void should_CheckJDKBelonging() {
        var reflectionJDK = ctx.getReflectionJDK();
        var reflectionNonJDK = ctx.getReflectionOrigin();

        assertTrue(ClassInfoModel.isJDKClass(reflectionJDK));
        assertTrue(ClassInfoModel.isJDKClass(reflectionJDK.getName()));
        assertTrue(ClassInfoModel.of(reflectionJDK).isJDKClass());

        assertTrue(ClassInfoModel.isNonJDKClass(reflectionNonJDK));
        assertTrue(ClassInfoModel.isNonJDKClass(reflectionNonJDK.getName()));
        assertTrue(ClassInfoModel.of(reflectionNonJDK).isNonJDKClass());
    }

    @DisplayName("It should be able to compare model with classes and other models")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_CompareClasses(ClassInfoModel model) {
        var sameReflectionClass = Dependency.Sample.class;
        var anotherReflectionClass = Dependency.Parent.class;
        var sameClassName = sameReflectionClass.getName();
        var anotherClassName = anotherReflectionClass.getName();

        assertTrue(model.is(sameClassName));
        assertTrue(model.is(sameReflectionClass));
        assertTrue(model.is(ClassInfoModel.of(sameReflectionClass)));

        assertFalse(model.is(anotherClassName));
        assertFalse(model.is(anotherReflectionClass));
        assertFalse(model.is(ClassInfoModel.of(anotherReflectionClass)));
    }

    @DisplayName("It should get all inner classes of the class")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetAllInnerClasses(ClassInfoModel model) {
        var expected = ClassMemberUtils
                .getDeclaredClasses(Dependency.Sample.class)
                .map(ClassInfoModel::of).collect(Collectors.toSet());
        var actual = new HashSet<>(model.getInnerClasses());

        assertEquals(expected, actual);
    }

    @DisplayName("It should get all fields of the class")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetClassFields(ClassInfoModel model) {
        var expected = getDeclaredFields(Dependency.Sample.class)
                .map(FieldInfoModel::of).collect(Collectors.toList());
        var actual = model.getFields();

        assertEquals(expected, actual);
    }

    @DisplayName("It should get the whole inheritance chain of the class")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetClassInheritanceChain(ClassInfoModel model) {
        var expected = Stream
                .<Class<?>> iterate(Dependency.Sample.class,
                        ((Predicate<Class<?>>) Objects::nonNull)
                                .and(ClassInfoModel::isNonJDKClass),
                        Class::getSuperclass)
                .map(ClassInfoModel::of).collect(Collectors.toList());
        var actual = model.getInheritanceChain();

        assertEquals(expected, actual);
    }

    @DisplayName("It should get all methods of the class")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetClassMethods(ClassInfoModel model) {
        var expected = getDeclaredMethods(Dependency.Sample.class)
                .map(MethodInfoModel::of).collect(Collectors.toSet());
        var actual = cleanup(model.getMethods().stream())
                .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @DisplayName("It should get interfaces the class implements")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetInterfaces(ClassInfoModel model) {
        var expected = Arrays.stream(Dependency.Sample.class.getInterfaces())
                .map(ClassInfoModel::of).map(ClassInfoModel::getName)
                .collect(Collectors.toList());
        var actual = model.getInterfaces().stream()
                .map(ClassRefSignatureModel::getName)
                .collect(Collectors.toList());

        assertEquals(expected, actual);
    }

    @DisplayName("It should get simple name of the class")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetSimpleName(ClassInfoModel model) {
        assertEquals(Dependency.Sample.class.getSimpleName(),
                model.getSimpleName());
    }

    @DisplayName("It should get superclass of the class")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetSuperclass(ClassInfoModel model) {
        assertEquals(Dependency.Sample.class.getSuperclass().getName(),
                model.getSuperClass().map(ClassRefSignatureModel::getName)
                        .orElse(null));
    }

    @DisplayName("It should have the same hashCode for models of the same origin")
    @Test
    public void should_HaveSameHashCodeForModelsOfSameOrigin() {
        var model = ClassInfoModel.of(ctx.getReflectionOrigin());
        var other = ClassInfoModel.of(ctx.getReflectionOrigin());

        assertEquals(model.hashCode(), other.hashCode());
    }

    @DisplayName("It should have models of the same origin equal")
    @Test
    public void should_HaveModelsOfSameOriginEqual() {
        var model = ClassInfoModel.of(ctx.getReflectionOrigin());
        var other = ClassInfoModel.of(ctx.getReflectionOrigin());

        assertEquals(model, model);
        assertEquals(model, other);
        assertEquals(other, model);

        assertNotEquals(model, new Object());
    }

    @DisplayName("It should provide correct origin")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectOrigin(ClassInfoModel model) {
        assertEquals(ctx.getReflectionOrigin(), model.get());
    }

    static final class Characteristics {
        enum Enum {
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE)
        @interface Annotation {
        }

        interface Interface {
        }

        static final class Final {
        }

        public static class Public {
        }

        protected static class Protected {
        }

        static abstract class Abstract {
        }

        private static class Private {
        }
    }

    static final class Dependency {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE)
        @interface Annotation {
        }

        interface Interface<TypeArgument extends ParametrizedDependency> {
        }

        static class FieldPrivate {
        }

        static class FieldProtected extends FieldSuper {
        }

        static class FieldPublic {
        }

        static class FieldStaticPrivate {
        }

        static class FieldStaticProtected {
        }

        static class FieldStaticPublic extends FieldStaticSuper {
        }

        static class FieldStaticSuper {
        }

        static class FieldSuper {
        }

        static class GrandParent {
            private GrandParentMethodPrivate methodPrivate() {
                return null;
            }
        }

        static class GrandParentMethodPrivate {
        }

        static class InnerField {
        }

        static class InnerMethod {
        }

        static class InnerParent {
        }

        static class MethodParent {
        }

        static class MethodPrivate {
        }

        static class MethodProtected extends MethodParent {
        }

        static class MethodPublic {
        }

        static class MethodStaticParent {
        }

        static class MethodStaticPrivate extends MethodStaticParent {
        }

        static class MethodStaticProtected {
        }

        static class MethodStaticPublic {
        }

        static class ParametrizedDependency {
        }

        static class Parent extends GrandParent {
            public ParentFieldPublic fieldPublic;
        }

        static class ParentFieldPublic {
        }

        @Annotation
        static class Sample extends Parent
                implements Interface<ParametrizedDependency> {
            public static FieldStaticPublic fieldStaticPublic;
            protected static FieldStaticProtected fieldStaticProtected;
            private static FieldStaticPrivate fieldStaticPrivate;
            public FieldPublic fieldPublic;
            protected FieldProtected fieldProtected;
            private FieldPrivate fieldPrivate;

            public static MethodStaticPublic methodStaticPublic() {
                return null;
            }

            protected static MethodStaticProtected methodStaticProtected() {
                return null;
            }

            private static MethodStaticPrivate methodStaticPrivate() {
                return null;
            }

            public MethodPublic methodPublic() {
                return null;
            }

            protected MethodProtected methodProtected() {
                return null;
            }

            private MethodPrivate methodPrivate() {
                return null;
            }

            public class DynamicInner {
                protected InnerMethod innerMethod() {
                    return null;
                }
            }

            public static class StaticInner extends InnerParent {
                public InnerField innerField;
            }
        }

        static class SampleChild extends Sample {
        }
    }

    static final class Specialization {
        private static class Sample {
            public Boolean getBoolean() {
                return true;
            }

            public Byte getByte() {
                return 0;
            }

            public Character getCharacter() {
                return 'a';
            }

            public Double getDouble() {
                return 0.0;
            }

            public Float getFloat() {
                return 0.0f;
            }

            public Integer getInteger() {
                return 0;
            }

            public Long getLong() {
                return 0L;
            }

            public Short getShort() {
                return 0;
            }

            public Void getVoid() {
                return null;
            }
        }
    }

    @Nested
    @DisplayName("As an AnnotatedModel")
    public class AsAnnotatedModel {
        @DisplayName("It should get a class annotation")
        @ParameterizedTest(name = ModelProvider.testNamePattern)
        @ArgumentsSource(ModelProvider.class)
        public void should_GetClassAnnotation(ClassInfoModel model) {
            assertEquals(List.of(AnnotationInfoModel.of(ctx.getAnnotation())),
                    model.getAnnotations());
        }
    }

    @DisplayName("As a class model with characteristics")
    @Nested
    public class AsCharacterizedClassModel {
        private final ModelProvider.CharacteristicsChecker checker = new ModelProvider.CharacteristicsChecker();

        @DisplayName("It should detect class characteristics correctly")
        @ParameterizedTest(name = ModelProvider.Characteristics.testNamePattern)
        @ArgumentsSource(ModelProvider.Characteristics.class)
        public void should_DetectCharacteristics(ClassInfoModel model,
                String[] characteristics, String testName) {
            checker.apply(model, characteristics);
        }
    }

    @Nested
    @DisplayName("As a SpecializedModel")
    public class AsSpecializedModel {
        private final ModelProvider.Checker checker = new ModelProvider.Checker();

        @DisplayName("It should detect class specialization correctly")
        @ParameterizedTest(name = ModelProvider.Specialization.testNamePattern)
        @ArgumentsSource(ModelProvider.Specialization.class)
        public void should_DetectClassSpecialization(ClassInfoModel model,
                String[] specializations, String testName) {
            checker.apply(model, specializations);
        }
    }

    public static class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{0}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context.Default();

            return Stream.of(
                    Arguments.of(ClassInfoModel.of(ctx.getReflectionOrigin())));
        }

        static final class Characteristics implements ArgumentsProvider {
            public static final String testNamePattern = "{2}";

            @Override
            public Stream<Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context.Characteristics();

                return ctx.getReflectionCharacteristics().entrySet().stream()
                        .map(entry -> Arguments.of(
                                ClassInfoModel.of(entry.getKey()),
                                entry.getValue(),
                                entry.getKey().getSimpleName()));
            }

        }

        static final class CharacteristicsChecker
                extends SpecializationChecker<ClassInfoModel> {
            private static final List<String> allowedMethods = List.of(
                    "isAbstract", "isAnnotation", "isArrayClass", "isEnum",
                    "isFinal", "isInterface", "isInterfaceOrAnnotation",
                    "isNative", "isPrivate", "isProtected", "isPublic",
                    "isStandardClass", "isStatic", "isSynthetic");

            public CharacteristicsChecker() {
                super(ClassInfoModel.class,
                        getDeclaredMethods(ClassInfoModel.class),
                        allowedMethods);
            }
        }

        static final class Checker
                extends SpecializationChecker<SpecializedModel> {
            public Checker() {
                super(SpecializedModel.class,
                        getDeclaredMethods(SpecializedModel.class));
            }
        }

        static final class Specialization implements ArgumentsProvider {
            public static final String testNamePattern = "{2}";

            @Override
            public Stream<Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context.Specializations();

                return ctx.getReflectionSpecializations().entrySet().stream()
                        .map(entry -> Arguments.of(
                                ClassInfoModel.of(entry.getKey()),
                                entry.getValue(),
                                entry.getKey().getSimpleName()));
            }
        }
    }

    static abstract class Context {
        static class Characteristics extends Context {
            private static final Map<Class<?>, String[]> reflectionCharacteristics = Map
                    .ofEntries(entry(
                            ClassInfoModelTests.Characteristics.Abstract.class,
                            "isAbstract", "isStandardClass", "isStatic"),
                            entry(ClassInfoModelTests.Characteristics.Annotation.class,
                                    "isAnnotation", "isAbstract",
                                    "isInterfaceOrAnnotation", "isStatic"),
                            entry(Object[].class, "isAbstract", "isArrayClass",
                                    "isFinal", "isPublic", "isStandardClass"),
                            entry(ClassInfoModelTests.Characteristics.Enum.class,
                                    "isEnum", "isFinal", "isStandardClass",
                                    "isStatic"),
                            entry(ClassInfoModelTests.Characteristics.Final.class,
                                    "isFinal", "isStandardClass", "isStatic"),
                            entry(ClassInfoModelTests.Characteristics.Interface.class,
                                    "isAbstract", "isInterface",
                                    "isInterfaceOrAnnotation", "isStatic"),
                            entry(Byte.class, "isFinal", "isPublic",
                                    "isStandardClass"),
                            entry(ClassInfoModelTests.Characteristics.Private.class,
                                    "isPrivate", "isStandardClass", "isStatic"),
                            entry(ClassInfoModelTests.Characteristics.Protected.class,
                                    "isProtected", "isStandardClass",
                                    "isStatic"),
                            entry(ClassInfoModelTests.Characteristics.Public.class,
                                    "isPublic", "isStandardClass", "isStatic"));

            public Map<Class<?>, String[]> getReflectionCharacteristics() {
                return reflectionCharacteristics;
            }
        }

        static class Default extends Context {
            private static final Annotation annotation = Dependency.Sample.class
                    .getAnnotation(Dependency.Annotation.class);
            private static final Class<?> reflectionJDK = List.class;
            private static final Class<?> reflectionOrigin = Dependency.Sample.class;

            public Annotation getAnnotation() {
                return annotation;
            }

            public Class<?> getReflectionJDK() {
                return reflectionJDK;
            }

            public Class<?> getReflectionOrigin() {
                return reflectionOrigin;
            }
        }

        static class Specializations extends Context {
            private static final Map<Class<?>, String[]> reflectionSpecializations = Map
                    .ofEntries(
                            entry(BigDecimal.class, "isJDKClass",
                                    "isBigDecimal"),
                            entry(BigInteger.class, "isJDKClass",
                                    "isBigInteger"),
                            entry(Boolean.class, "isJDKClass", "isBoolean"),
                            entry(Byte.class, "isJDKClass", "isByte",
                                    "hasIntegerType"),
                            entry(Character.class, "isJDKClass", "isCharacter"),
                            entry(Double.class, "isJDKClass", "isDouble",
                                    "hasFloatType"),
                            entry(Float.class, "isJDKClass", "isFloat",
                                    "hasFloatType"),
                            entry(Integer.class, "isJDKClass", "isInteger",
                                    "hasIntegerType"),
                            entry(Long.class, "isJDKClass", "isLong",
                                    "hasIntegerType"),
                            entry(Short.class, "isJDKClass", "isShort",
                                    "hasIntegerType"),
                            entry(Void.class, "isJDKClass"));

            public Map<Class<?>, String[]> getReflectionSpecializations() {
                return reflectionSpecializations;
            }
        }
    }

}
