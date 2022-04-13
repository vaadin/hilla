package dev.hilla.parser.models;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedArrayType;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.hilla.parser.test.helpers.TestHelper;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.ScanResult;

public class ArraySignatureModelTests {
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

        @BeforeEach
        public void setUp(@Mock Model parent) throws NoSuchMethodException {
            var origin = (AnnotatedArrayType) Sample.class.getMethod("foo")
                    .getAnnotatedReturnType();

            model = ArraySignatureModel.of(origin, parent);
        }

        @Test
        public void should_CreateCorrectModel_When_JavaReflectionUsed() {
            assertTrue(model.isReflection());
        }

        @Test
        public void should_GetNestedType() {
            checkNestedType(model);
        }
    }

    @ExtendWith(MockitoExtension.class)
    public static class SourceModelTests {
        private static final TestHelper helper = new TestHelper();
        private static ScanResult result;
        private ArraySignatureModel model;

        @AfterAll
        public static void destroy() {
            result.close();
        }

        @BeforeAll
        public static void init() {
            result = helper.createClassGraph().scan();
        }

        @BeforeEach
        public void setUp(@Mock Model parent) {
            var origin = (ArrayTypeSignature) result
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
        }

        @Test
        public void should_GetNestedType() {
            checkNestedType(model);
        }
    }

    @Selector
    private static class Sample {
        public String[] foo() {
            return new String[] {};
        }
    }
}
