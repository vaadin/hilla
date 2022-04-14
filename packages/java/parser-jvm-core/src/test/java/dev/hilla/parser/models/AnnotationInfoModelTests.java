package dev.hilla.parser.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.classgraph.AnnotationInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.hilla.parser.test.helpers.SourceHelper;

@ExtendWith(MockitoExtension.class)
public class AnnotationInfoModelTests {
    private static void checkModelProvidingName(AnnotationInfoModel model) {
        assertEquals(model.getName(), Foo.class.getName());
    }

    private static void checkModelProvidingNoDependencies(
            AnnotationInfoModel model) {
        assertEquals(model.getDependencies().size(), 0);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Foo {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Selector {
    }

    public static class ReflectionModelTests {
        private AnnotationInfoModel model;
        private Annotation origin;

        @BeforeEach
        public void setUp(@Mock Model parent) throws NoSuchMethodException {
            origin = Sample.class.getMethod("bar").getAnnotation(Foo.class);
            model = AnnotationInfoModel.of(origin, parent);
        }

        @Test
        public void should_CreateCorrectModel_When_JavaReflectionUsed() {
            assertTrue(model.isReflection());
            assertEquals(model.get(), origin);
        }

        @Test
        public void should_ProvideNoDependencies() {
            checkModelProvidingNoDependencies(model);
        }

        @Nested
        public class AsNamedModel {
            @Test
            public void should_HaveName() {
                checkModelProvidingName(model);
            }
        }
    }

    public static class SourceModelTests {
        private static final SourceHelper helper = new SourceHelper();
        private AnnotationInfoModel model;
        private AnnotationInfo origin;

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
            origin = helper.getScanResult().getClassesWithAnnotation(Selector.class)
                    .stream().flatMap(cls -> cls.getMethodInfo().stream())
                    .flatMap(method -> method.getAnnotationInfo().stream())
                    .findFirst().get();

            model = AnnotationInfoModel.of(origin, parent);
        }

        @Test
        public void should_ProvideNoDependencies() {
            checkModelProvidingNoDependencies(model);
        }

        @Test
        public void should_createCorrectModel_When_ClassGraphUsed() {
            assertTrue(model.isSource());
            assertEquals(model.get(), origin);
        }

        @Nested
        public class AsNamedModel {
            @Test
            public void should_HaveName() {
                checkModelProvidingName(model);
            }
        }
    }

    @Selector
    static class Sample {
        @Foo
        public void bar() {
        }
    }
}
