package dev.hilla.parser.models.classinfomodel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.test.helpers.BaseTestContext;
import dev.hilla.parser.test.helpers.ModelKind;
import dev.hilla.parser.test.helpers.ParserExtension;

@ExtendWith(ParserExtension.class)
public class ClassInfoModelKindTests {
    @DisplayName("It should detect abstract class")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(KindAbstractProvider.class)
    public void should_DetectAbstractClass(ClassInfoModel model, ModelKind kind,
            TestContext context) {
        assertTrue(model.isAbstract());
    }

    @DisplayName("It should detect annotation interface")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(KindAnnotationProvider.class)
    public void should_DetectAnnotationInterface(ClassInfoModel model,
            ModelKind kind, TestContext context) {
        assertTrue(model.isAnnotation());
        assertTrue(model.isInterfaceOrAnnotation());
    }

    @DisplayName("It should detect dynamic class")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(KindDynamicProvider.class)
    public void should_DetectDynamicClass(ClassInfoModel model, ModelKind kind,
            TestContext context) {
        assertFalse(model.isStatic());
    }

    @DisplayName("It should detect final class")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(KindFinalProvider.class)
    public void should_DetectFinalClass(ClassInfoModel model, ModelKind kind,
            TestContext context) {
        assertTrue(model.isFinal());
    }

    @DisplayName("It should detect interface")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(KindInterfaceProvider.class)
    public void should_DetectInterface(ClassInfoModel model, ModelKind kind,
            TestContext context) {
        assertTrue(model.isInterface());
        assertTrue(model.isInterfaceOrAnnotation());
    }

    @DisplayName("It should detect private class")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(KindPrivateProvider.class)
    public void should_DetectPrivateClass(ClassInfoModel model, ModelKind kind,
            TestContext context) {
        assertTrue(model.isPrivate());
    }

    @DisplayName("It should detect protected class")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(KindProtectedProvider.class)
    public void should_DetectProtectedClass(ClassInfoModel model,
            ModelKind kind, TestContext context) {
        assertTrue(model.isProtected());
    }

    @DisplayName("It should detect public class")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(KindPublicProvider.class)
    public void should_DetectPublicClass(ClassInfoModel model, ModelKind kind,
            TestContext context) {
        assertTrue(model.isPublic());
    }

    @DisplayName("It should detect static class")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(KindStaticProvider.class)
    public void should_DetectStaticClass(ClassInfoModel model, ModelKind kind,
            TestContext context) {
        assertTrue(model.isStatic());
    }

    public static final class KindAbstractProvider extends ModelProvider {
        @Override
        protected Class<?> getKindClass() {
            return Abstract.class;
        }

        private static abstract class Abstract {
        }
    }

    public static final class KindAnnotationProvider extends ModelProvider {
        @Override
        protected Class<?> getKindClass() {
            return Annotation.class;
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE)
        private @interface Annotation {
        }
    }

    public static final class KindDynamicProvider extends ModelProvider {
        @Override
        protected Class<?> getKindClass() {
            return Dynamic.class;
        }

        private class Dynamic {
        }
    }

    public static final class KindFinalProvider extends ModelProvider {
        @Override
        protected Class<?> getKindClass() {
            return Final.class;
        }

        private static final class Final {
        }
    }

    public static final class KindInterfaceProvider extends ModelProvider {
        @Override
        protected Class<?> getKindClass() {
            return Interface.class;
        }

        private interface Interface {
        }
    }

    public static final class KindPrivateProvider extends ModelProvider {
        @Override
        protected Class<?> getKindClass() {
            return Private.class;
        }

        private static class Private {
        }
    }

    public static final class KindProtectedProvider extends ModelProvider {
        @Override
        protected Class<?> getKindClass() {
            return Protected.class;
        }

        protected static class Protected {
        }
    }

    public static final class KindPublicProvider extends ModelProvider {
        @Override
        protected Class<?> getKindClass() {
            return Public.class;
        }

        public static class Public {
        }
    }

    public static final class KindStaticProvider extends ModelProvider {
        @Override
        protected Class<?> getKindClass() {
            return Static.class;
        }

        private static class Static {
        }
    }

    public static abstract class ModelProvider implements ArgumentsProvider {
        public static final String testName = "{1}";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) {
            var ctx = new TestContext(getKindClass(), context);

            return Stream.of(ctx.getReflectionArguments(),
                    ctx.getSourceArguments());
        }

        protected abstract Class<?> getKindClass();
    }

    private static class TestContext extends BaseTestContext {
        private final Class<?> kind;

        public TestContext(Class<?> kind, ExtensionContext context) {
            super(context);
            this.kind = kind;
        }

        public Arguments getReflectionArguments() {
            var model = ClassInfoModel.of(kind);

            return Arguments.of(model, ModelKind.REFLECTION, this);
        }

        public Arguments getSourceArguments() {
            var origin = getScanResult().getClassInfo(kind.getName());
            var model = ClassInfoModel.of(origin);

            return Arguments.of(model, ModelKind.SOURCE, this);
        }
    }
}
