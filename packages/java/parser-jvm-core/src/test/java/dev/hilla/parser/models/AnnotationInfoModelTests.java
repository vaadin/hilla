package dev.hilla.parser.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.hilla.parser.test.helpers.BaseTestContext;
import dev.hilla.parser.test.helpers.ModelKind;
import dev.hilla.parser.test.helpers.ParserExtension;
import dev.hilla.parser.test.helpers.WithScanResult;

import io.github.classgraph.ScanResult;

@ExtendWith(ParserExtension.class)
public class AnnotationInfoModelTests {
    @DisplayName("It should create correct model")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_CreateCorrectModel(AnnotationInfoModel model,
            Object origin, ModelKind kind, ModelProvider.Context context) {
        switch (kind) {
        case REFLECTION:
            assertEquals(origin, model.get());
            assertTrue(model.isReflection());
            break;
        case SOURCE:
            assertEquals(origin, model.get());
            assertTrue(model.isSource());
            break;
        }
    }

    @DisplayName("It should have the same hashCode for source and reflection models")
    @Test
    public void should_HaveSameHashCodeForSourceAndReflectionModels(
            @WithScanResult ScanResult scanResult)
            throws NoSuchMethodException {
        var reflectionModel = getDefaultReflectionModel();
        var sourceModel = getDefaultSourceModel(scanResult);

        assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
    }

    @DisplayName("It should have the same hashCode for source and reflection models")
    @Test
    public void should_HaveSourceAndReflectionModelsEqual(
            @WithScanResult ScanResult scanResult)
            throws NoSuchMethodException {
        var reflectionModel = getDefaultReflectionModel();
        var sourceModel = getDefaultSourceModel(scanResult);

        assertEquals(reflectionModel, reflectionModel);
        assertEquals(reflectionModel, sourceModel);

        assertEquals(sourceModel, sourceModel);
        assertEquals(sourceModel, reflectionModel);

        assertNotEquals(sourceModel, new Object());
        assertNotEquals(reflectionModel, new Object());
    }

    @DisplayName("It should provide no dependencies")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideNoDependencies(AnnotationInfoModel model,
            Object origin, ModelKind kind, BaseTestContext context) {
        assertEquals(0, model.getDependencies().size());
    }

    private AnnotationInfoModel getDefaultReflectionModel()
            throws NoSuchMethodException {
        return AnnotationInfoModel.of(Sample.class.getDeclaredMethod("bar")
                .getAnnotation(Sample.Foo.class));
    }

    private AnnotationInfoModel getDefaultSourceModel(ScanResult scanResult) {
        return AnnotationInfoModel.of(scanResult
                .getClassInfo(Sample.class.getName()).getDeclaredMethodInfo("bar")
                .getSingleMethod("bar").getAnnotationInfo(Sample.Foo.class));
    }

    public static final class ModelProvider implements ArgumentsProvider {
        public static final String testName = "{1}";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) throws NoSuchMethodException {
            var ctx = new Context(context);

            return Stream.of(ctx.getReflectionArguments(),
                    ctx.getSourceArguments());
        }

        public static final class Context extends BaseTestContext {
            Context(ExtensionContext context) {
                super(context);
            }

            public Arguments getReflectionArguments()
                    throws NoSuchMethodException {
                var origin = Sample.class.getMethod("bar")
                        .getAnnotation(Sample.Foo.class);
                var model = AnnotationInfoModel.of(origin);

                return Arguments.of(model, origin, ModelKind.REFLECTION, this);
            }

            public Arguments getSourceArguments() {
                var origin = getScanResult()
                        .getClassInfo(Sample.class.getName())
                        .getDeclaredMethodInfo("bar").getSingleMethod("bar")
                        .getAnnotationInfo(Sample.Foo.class.getName());
                var model = AnnotationInfoModel.of(origin);

                return Arguments.of(model, origin, ModelKind.SOURCE, this);
            }
        }
    }

    @Nested
    @DisplayName("As a NamedModel")
    public class AsNamedModel {
        @DisplayName("It should have name")
        @ParameterizedTest(name = ModelProvider.testName)
        @ArgumentsSource(ModelProvider.class)
        public void should_HaveName(AnnotationInfoModel model, Object origin,
                ModelKind kind, BaseTestContext context) {
            assertEquals(Sample.Foo.class.getName(), model.getName());
        }
    }

    private static class Sample {
        @Foo
        public void bar() {
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        @interface Foo {
        }
    }
}
