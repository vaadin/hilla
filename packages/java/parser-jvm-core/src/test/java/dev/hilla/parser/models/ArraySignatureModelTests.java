package dev.hilla.parser.models;

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
import java.util.Set;
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
import dev.hilla.parser.test.helpers.SpecializationChecker;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.ScanResult;

@ExtendWith(SourceExtension.class)
public class ArraySignatureModelTests {
    private Context ctx;

    @BeforeEach
    public void setUp(@Source ScanResult source) throws NoSuchFieldException {
        ctx = new Context(source);
    }

    @DisplayName("It should create correct model")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_CreateCorrectModel(ArraySignatureModel model,
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
        var reflectionModel = ArraySignatureModel.of(ctx.getReflectionOrigin());
        var sourceModel = ArraySignatureModel.of(ctx.getSourceOrigin());

        assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
    }

    @DisplayName("It should have source and reflection models equal")
    @Test
    public void should_HaveSourceAndReflectionModelsEqual() {
        var reflectionModel = ArraySignatureModel.of(ctx.getReflectionOrigin());
        var sourceModel = ArraySignatureModel.of(ctx.getSourceOrigin());

        assertEquals(reflectionModel, reflectionModel);
        assertEquals(reflectionModel, sourceModel);

        assertEquals(sourceModel, sourceModel);
        assertEquals(sourceModel, reflectionModel);

        assertNotEquals(sourceModel, new Object());
        assertNotEquals(reflectionModel, new Object());
    }

    @DisplayName("It should provide dependencies")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideDependencies(ArraySignatureModel model,
            ModelKind kind) {
        var expected = Set.of(ClassInfoModel.of(Dependency.class));
        var actual = model.getDependencies();

        assertEquals(expected, actual);
    }

    @DisplayName("It should provide nested type")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideNestedType(ArraySignatureModel model,
            ModelKind kind) {
        var nested = model.getNestedType();
        assertTrue(nested instanceof ClassRefSignatureModel);

        switch (kind) {
        case REFLECTION: {
            var nestedOrigin = (AnnotatedType) nested.get();
            assertEquals(nestedOrigin.getType().getTypeName(),
                    Dependency.class.getName());
        }
            break;
        case SOURCE: {
            var nestedOrigin = (ClassRefTypeSignature) nested.get();
            assertEquals(nestedOrigin.getFullyQualifiedClassName(),
                    Dependency.class.getName());
        }
            break;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Bar {
    }

    static final class Context {
        private static final String fieldName = "foo";
        private final Annotation annotation;
        private final AnnotatedArrayType reflectionOrigin;
        private final ArrayTypeSignature sourceOrigin;

        Context(ExtensionContext context) throws NoSuchFieldException {
            this(SourceExtension.getSource(context));
        }

        Context(ScanResult source) throws NoSuchFieldException {
            reflectionOrigin = (AnnotatedArrayType) Sample.class
                    .getDeclaredField(fieldName).getAnnotatedType();
            sourceOrigin = (ArrayTypeSignature) source
                    .getClassInfo(Sample.class.getName())
                    .getDeclaredFieldInfo(fieldName)
                    .getTypeSignatureOrTypeDescriptor();
            annotation = Sample.class.getDeclaredField(fieldName)
                    .getAnnotation(Bar.class);
        }

        public Annotation getAnnotation() {
            return annotation;
        }

        public AnnotatedArrayType getReflectionOrigin() {
            return reflectionOrigin;
        }

        public ArrayTypeSignature getSourceOrigin() {
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
                            ArraySignatureModel.of(ctx.getReflectionOrigin()),
                            ModelKind.REFLECTION),
                    Arguments.of(ArraySignatureModel.of(ctx.getSourceOrigin()),
                            ModelKind.SOURCE));
        }

        static final class Checker
                extends SpecializationChecker<SpecializedModel> {
            Checker() {
                super(SpecializedModel.class,
                        SpecializedModel.class.getDeclaredMethods());
            }
        }
    }

    @Nested
    @DisplayName("As an AnnotatedModel")
    public class AsAnnotatedModel {
        @DisplayName("It should access annotations")
        @ParameterizedTest(name = ModelProvider.testName)
        @ArgumentsSource(ModelProvider.class)
        public void should_AccessAnnotations(ArraySignatureModel model,
                ModelKind kind) {
            assertEquals(List.of(AnnotationInfoModel.of(ctx.getAnnotation())),
                    model.getNestedType().getAnnotations());
        }
    }

    @Nested
    @DisplayName("As a SpecializedModel")
    public class AsSpecializedModel {
        private final ModelProvider.Checker checker = new ModelProvider.Checker();

        @DisplayName("It should have an array specialization")
        @ParameterizedTest(name = ModelProvider.testName)
        @ArgumentsSource(ModelProvider.class)
        public void should_HaveArraySpecialization(ArraySignatureModel model,
                ModelKind kind) {
            checker.apply(model, "isArray", "isNonJDKClass");
        }
    }

    static class Dependency {
    }

    static class Sample {
        @Bar
        private Dependency[] foo;
    }
}
