package dev.hilla.parser.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedArrayType;
import java.util.Objects;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.hilla.parser.test.helpers.SourceHelper;
import dev.hilla.parser.test.helpers.SpecializationHelper;

import io.github.classgraph.ArrayTypeSignature;

public class ArraySignatureModelTests {
    private static void specializationChecker(String name,
            Supplier<Boolean> checker) {
        if (Objects.equals(name, "isArray")
                || Objects.equals(name, "isNonJDKClass")) {
            assertTrue(checker.get());
        } else {
            assertFalse(checker.get(),
                    String.format("'%s' should return false", name));
        }
    }

    private static void checkNestedType(ArraySignatureModel model) {
        var nestedModel = model.getNestedType();
        assertTrue(nestedModel.isString());
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Selector {
    }

    @ExtendWith(MockitoExtension.class)
    public static class ReflectionModelTests {
        private ArraySignatureModel model;
        private AnnotatedArrayType origin;

        @BeforeEach
        public void setUp(@Mock Model parent) throws NoSuchMethodException {
            origin = (AnnotatedArrayType) Sample.class.getMethod("foo")
                    .getAnnotatedReturnType();

            model = ArraySignatureModel.of(origin, parent);
        }

        @Test
        public void should_CreateCorrectModel_When_JavaReflectionUsed() {
            assertTrue(model.isReflection());
            assertEquals(model.get(), origin);
        }

        @Test
        public void should_GetNestedType() {
            checkNestedType(model);
        }

        @Nested
        public class AsSpecializedModel {
            private SpecializationHelper specializationHelper;

            @BeforeEach
            public void setUp() {
                specializationHelper = new SpecializationHelper(model);
            }

            @Test
            public void should_HaveArraySpecialization() {
                specializationHelper
                        .apply(ArraySignatureModelTests::specializationChecker);
            }
        }
    }

    @ExtendWith(MockitoExtension.class)
    public static class SourceModelTests {
        private static final SourceHelper helper = new SourceHelper();
        private ArraySignatureModel model;
        private ArrayTypeSignature origin;

        @AfterAll
        public static void fin() {
            helper.fin();
        }

        @BeforeAll
        public static void init() {
            helper.init();
        }

        @BeforeEach
        public void setUp(@Mock Model parent) {
            origin = (ArrayTypeSignature) helper.getScanResult()
                    .getClassesWithAnnotation(Selector.class).stream()
                    .flatMap(cls -> cls.getMethodInfo().stream())
                    .map(method -> method.getTypeSignatureOrTypeDescriptor()
                            .getResultType())
                    .findFirst().get();

            model = ArraySignatureModel.of(origin, parent);
        }

        @Test
        public void should_CreateCorrectModel_When_ClassGraphUsed() {
            assertTrue(model.isSource());
            assertEquals(model.get(), origin);
        }

        @Test
        public void should_GetNestedType() {
            checkNestedType(model);
        }

        @Nested
        public class AsSpecializedModel {
            private SpecializationHelper specializationHelper;

            @BeforeEach
            public void setUp() {
                specializationHelper = new SpecializationHelper(model);
            }

            @Test
            public void should_HaveArraySpecialization() {
                specializationHelper
                        .apply(ArraySignatureModelTests::specializationChecker);
            }
        }
    }

    @Selector
    private static class Sample {
        public String[] foo() {
            return new String[] {};
        }
    }
}
