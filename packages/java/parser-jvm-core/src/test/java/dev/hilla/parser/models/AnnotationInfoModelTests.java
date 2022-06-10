package dev.hilla.parser.models;

import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.hilla.parser.test.helpers.ModelKind;
import dev.hilla.parser.test.helpers.Source;
import dev.hilla.parser.test.helpers.SourceExtension;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ScanResult;

@ExtendWith(SourceExtension.class)
public class AnnotationInfoModelTests {
    private Context ctx;

    @BeforeEach
    public void setUp(@Source ScanResult source) {
        ctx = new Context(source);
    }

    @DisplayName("It should create correct model")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_CreateCorrectModel(AnnotationInfoModel model,
            ModelKind kind) {
        switch (kind) {
        case REFLECTION:
            assertEquals(ctx.getReflectionOrigin(), model.get());
            assertTrue(model.isReflection());
            break;
        case SOURCE:
            assertEquals(ctx.getSourceOrigin(), model.get());
            assertTrue(model.isSource());
            break;
        }
    }

    @DisplayName("It should have the same hashCode for source and reflection models")
    @Test
    public void should_HaveSameHashCodeForSourceAndReflectionModels() {
        var reflectionModel = AnnotationInfoModel.of(ctx.getReflectionOrigin());
        var sourceModel = AnnotationInfoModel.of(ctx.getSourceOrigin());

        assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
    }

    @DisplayName("It should have source and reflection models equal")
    @Test
    public void should_HaveSourceAndReflectionModelsEqual() {
        var reflectionModel = AnnotationInfoModel.of(ctx.getReflectionOrigin());
        var sourceModel = AnnotationInfoModel.of(ctx.getSourceOrigin());

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
            ModelKind kind) {
        assertEquals(0, model.getDependencies().size());
    }

    static final class Context {
        private static final String fieldName = "bar";
        private static final Annotation reflectionOrigin = getDeclaredField(
                Sample.class, fieldName).getAnnotation(Sample.Foo.class);
        private final AnnotationInfo sourceOrigin;

        Context(ExtensionContext context) {
            this(SourceExtension.getSource(context));
        }

        Context(ScanResult source) {
            sourceOrigin = getDeclaredField(Sample.class, fieldName, source)
                    .getAnnotationInfo(Sample.Foo.class);
        }

        public Annotation getReflectionOrigin() {
            return reflectionOrigin;
        }

        public AnnotationInfo getSourceOrigin() {
            return sourceOrigin;
        }
    }

    static final class ModelProvider implements ArgumentsProvider {
        public static final String testName = "{1}";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) throws NoSuchFieldException {
            var ctx = new Context(context);

            return Stream.of(
                    Arguments.of(
                            AnnotationInfoModel.of(ctx.getReflectionOrigin()),
                            ModelKind.REFLECTION),
                    Arguments.of(AnnotationInfoModel.of(ctx.getSourceOrigin()),
                            ModelKind.SOURCE));
        }
    }

    static final class Sample {
        @Foo
        private String bar;

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.FIELD)
        @interface Foo {
        }
    }

    @Nested
    @DisplayName("As a NamedModel")
    public class AsNamedModel {
        @DisplayName("It should have name")
        @ParameterizedTest(name = ModelProvider.testName)
        @ArgumentsSource(ModelProvider.class)
        public void should_HaveName(AnnotationInfoModel model, ModelKind kind) {
            assertEquals(Sample.Foo.class.getName(), model.getName());
        }
    }
}
