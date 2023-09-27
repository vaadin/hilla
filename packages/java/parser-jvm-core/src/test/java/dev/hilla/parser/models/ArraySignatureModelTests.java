package dev.hilla.parser.models;

import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredField;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethods;
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
    public void setUp(@Source ScanResult source) {
        ctx = new Context(source);
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

    @DisplayName("It should provide correct origin")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectOrigin(ArraySignatureModel model,
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

    @DisplayName("It should provide nested type")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
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

    static final class Context {
        private static final String fieldName = "foo";
        private static final Annotation annotation = ((AnnotatedArrayType) getDeclaredField(
                Sample.class, fieldName).getAnnotatedType())
                .getAnnotatedGenericComponentType()
                .getAnnotation(Sample.Bar.class);
        private static final AnnotatedArrayType reflectionOrigin = (AnnotatedArrayType) getDeclaredField(
                Sample.class, fieldName).getAnnotatedType();
        private final ArrayTypeSignature sourceOrigin;

        Context(ExtensionContext context) {
            this(SourceExtension.getSource(context));
        }

        Context(ScanResult source) {
            sourceOrigin = (ArrayTypeSignature) getDeclaredField(Sample.class,
                    fieldName, source).getTypeSignatureOrTypeDescriptor();
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
        public static final String testNamePattern = "{1}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
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
                        getDeclaredMethods(SpecializedModel.class));
            }
        }
    }

    @Nested
    @DisplayName("As an AnnotatedModel")
    public class AsAnnotatedModel {
        @DisplayName("It should access annotations")
        @ParameterizedTest(name = ModelProvider.testNamePattern)
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
        @ParameterizedTest(name = ModelProvider.testNamePattern)
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

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE_USE)
        @interface Bar {
        }
    }
}
