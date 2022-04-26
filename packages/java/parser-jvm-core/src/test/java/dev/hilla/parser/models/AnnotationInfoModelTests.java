package dev.hilla.parser.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.hilla.parser.test.helpers.BaseTestContext;
import dev.hilla.parser.test.helpers.ModelKind;
import dev.hilla.parser.test.helpers.ParserExtension;

import io.github.classgraph.AnnotationInfo;

@ExtendWith(ParserExtension.class)
public class AnnotationInfoModelTests {
    @DisplayName("It should create correct model")
    @ParameterizedTest(name = "{1}")
    @ArgumentsSource(ModelProvider.class)
    public void should_CreateCorrectModel(AnnotationInfoModel model,
            ModelKind kind, TestContext context) throws NoSuchMethodException {
        switch (kind) {
        case REFLECTION: {
            var origin = context.getReflectionOrigin();
            assertEquals(origin, model.get());
            assertTrue(model.isReflection());
        }
            break;
        case SOURCE: {
            var origin = context.getSourceOrigin();
            assertEquals(origin, model.get());
            assertTrue(model.isSource());
        }
            break;
        }
    }

    @DisplayName("It should provide no dependencies")
    @ParameterizedTest(name = "{1}")
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideNoDependencies(AnnotationInfoModel model,
            ModelKind kind, BaseTestContext context) {
        assertEquals(0, model.getDependencies().size());
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Foo {
    }

    private static final class TestContext extends BaseTestContext {
        public TestContext(ExtensionContext context) {
            super(context);
        }

        public Arguments getReflectionArguments() throws NoSuchMethodException {
            var origin = getReflectionOrigin();
            var model = AnnotationInfoModel.of(origin, mock(Model.class));

            return Arguments.of(model, ModelKind.REFLECTION, this);
        }

        public Annotation getReflectionOrigin() throws NoSuchMethodException {
            return Sample.class.getMethod("bar").getAnnotation(Foo.class);
        }

        public Arguments getSourceArguments() {
            var origin = getSourceOrigin();
            var model = AnnotationInfoModel.of(origin, mock(Model.class));

            return Arguments.of(model, ModelKind.SOURCE, this);
        }

        public AnnotationInfo getSourceOrigin() {
            return getScanResult().getClassInfo(Sample.class.getName())
                    .getMethodInfo("bar").getSingleMethod("bar")
                    .getAnnotationInfo(Foo.class.getName());
        }
    }

    @Nested
    @DisplayName("As a NamedModel")
    public class AsNamedModel {
        @DisplayName("It should have name")
        @ParameterizedTest(name = "{1}")
        @ArgumentsSource(ModelProvider.class)
        public void should_HaveName(AnnotationInfoModel model, ModelKind kind,
                BaseTestContext context) {
            assertEquals(Foo.class.getName(), model.getName());
        }
    }

    public static class ModelProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) throws NoSuchMethodException {
            var ctx = new TestContext(context);

            return Stream.of(ctx.getReflectionArguments(),
                    ctx.getSourceArguments());
        }
    }

    private static class Sample {
        @Foo
        public void bar() {
        }
    }
}
