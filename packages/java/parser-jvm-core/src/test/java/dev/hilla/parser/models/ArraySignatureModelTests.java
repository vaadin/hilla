package dev.hilla.parser.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.hilla.parser.test.helpers.BaseTestContext;
import dev.hilla.parser.test.helpers.ModelKind;
import dev.hilla.parser.test.helpers.ParserExtension;
import dev.hilla.parser.test.helpers.SpecializationChecker;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.ClassRefTypeSignature;

@ExtendWith(ParserExtension.class)
public class ArraySignatureModelTests {
    @DisplayName("It should create correct model")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_CreateCorrectModel(ArraySignatureModel model,
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

    @DisplayName("It should provide dependencies")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideDependencies(ArraySignatureModel model,
            Object origin, ModelKind kind, ModelProvider.Context context) {
        var expected = Set.of(ClassInfoModel.of(Dependency.class));
        var actual = model.getDependencies();

        assertEquals(expected, actual);
    }

    @DisplayName("It should provide nested type")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideNestedType(ArraySignatureModel model,
            Object origin, ModelKind kind, ModelProvider.Context context) {
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
    @Target(ElementType.TYPE_USE)
    @interface Bar {
    }

    @Nested
    @DisplayName("As an AnnotatedModel")
    public class AsAnnotatedModel {
        private AnnotationInfoModel annotation;

        @BeforeEach
        public void setUp() throws NoSuchMethodException {
            var annotationOrigin = ((AnnotatedArrayType) Sample.class
                    .getMethod("foo").getAnnotatedReturnType())
                            .getAnnotatedGenericComponentType()
                            .getAnnotation(Bar.class);

            annotation = AnnotationInfoModel.of(annotationOrigin);
        }

        @DisplayName("It should access annotations")
        @ParameterizedTest(name = ModelProvider.testName)
        @ArgumentsSource(ModelProvider.class)
        public void should_AccessAnnotations(ArraySignatureModel model,
                Object origin, ModelKind kind, ModelProvider.Context context) {
            // Annotation is added to String, not String[]; so we need to
            // extract nested type to get the type annotation
            assertEquals(List.of(annotation),
                    model.getNestedType().getAnnotations());
        }
    }

    @Nested
    @DisplayName("As a SpecializedModel")
    public class AsSpecializedModel {
        private final SpecializationChecker<SpecializedModel> checker = new SpecializationChecker<>(
                SpecializedModel.class,
                SpecializedModel.class.getDeclaredMethods());

        @DisplayName("It should have an array specialization")
        @ParameterizedTest(name = ModelProvider.testName)
        @ArgumentsSource(ModelProvider.class)
        public void should_HaveArraySpecialization(ArraySignatureModel model,
                Object origin, ModelKind kind, ModelProvider.Context context) {
            checker.apply(model, "isArray", "isNonJDKClass");
        }
    }

    public static class ModelProvider implements ArgumentsProvider {
        public static final String testName = "{2}";

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
                var origin = (AnnotatedArrayType) Sample.class
                        .getDeclaredMethod("foo").getAnnotatedReturnType();
                var model = ArraySignatureModel.of(origin);

                return Arguments.of(model, origin, ModelKind.REFLECTION, this);
            }

            public Arguments getSourceArguments() {
                var origin = (ArrayTypeSignature) getScanResult()
                        .getClassInfo(Sample.class.getName())
                        .getMethodInfo("foo").getSingleMethod("foo")
                        .getTypeSignatureOrTypeDescriptor().getResultType();
                var model = ArraySignatureModel.of(origin);

                return Arguments.of(model, origin, ModelKind.SOURCE, this);
            }
        }
    }

    private static class Dependency {
    }

    private static class Sample {
        @Bar
        public Dependency[] foo() {
            return null;
        }
    }
}
