package dev.hilla.parser.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.hilla.parser.misc.TestHelper;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ScanResult;

public class AnnotationInfoModelTests {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Foo {
    }

    @ExtendWith(MockitoExtension.class)
    public static class ReflectionModelTests {
        Annotation origin;
        @Mock
        Model parent;

        @BeforeEach
        public void setUp() throws NoSuchMethodException {
            origin = Sample.class.getMethod("bar").getAnnotation(Foo.class);
        }

        @Test
        public void should_CreateCorrectModel_When_JavaReflectionUsed() {
            var model = AnnotationInfoModel.of(origin, parent);
            assertTrue(model.isReflection());
            assertEquals(model.getName(), Foo.class.getName());
        }

        @Test
        public void should_ProvideNoDependencies() {
            var model = AnnotationInfoModel.of(origin, parent);
            assertEquals(model.getDependencies().size(), 0);
        }
    }

    @ExtendWith(MockitoExtension.class)
    public static class SourceModelTests {
        private static final TestHelper helper = new TestHelper();
        private static ScanResult result;
        private AnnotationInfo origin;
        @Mock
        private Model parent;

        @AfterAll
        public static void destroy() {
            result.close();
        }

        @BeforeAll
        public static void init() {
            result = helper.createClassGraph().scan();
        }

        @BeforeEach
        public void setUp() {
            origin = result.getClassesWithMethodAnnotation(Foo.class.getName())
                    .stream().flatMap(cls -> cls.getMethodInfo().stream())
                    .flatMap(method -> method.getAnnotationInfo().stream())
                    .findFirst().get();
        }

        @Test
        public void should_ProvideNoDependencies() {
            var model = AnnotationInfoModel.of(origin, parent);
            assertEquals(model.getDependencies().size(), 0);
        }

        @Test
        public void should_createCorrectModel_When_ClassGraphUsed() {
            var model = AnnotationInfoModel.of(origin, parent);
            assertTrue(model.isSource());
            assertEquals(model.getName(), Foo.class.getName());
        }
    }

    static class Sample {
        @Foo
        public void bar() {
        }
    }
}
