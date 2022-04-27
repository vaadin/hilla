package dev.hilla.parser.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

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

    @DisplayName("It should provide dependencies")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideDependencies(ArraySignatureModel model,
            ModelKind kind, BaseTestContext context) {
        var dependencies = model.getDependencies();

        assertEquals(
                Set.of(ClassInfoModel.of(Dependency.class, mock(Model.class))),
                dependencies);
    }

    @DisplayName("It should provide nested type")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideNestedType(ArraySignatureModel model,
            ModelKind kind, BaseTestContext context) {
        var nested = model.getNestedType();
        assertTrue(nested instanceof ClassRefSignatureModel);

        switch (kind) {
        case REFLECTION: {
            var nestedOrigin = (AnnotatedType) nested.get();
            var cls = (Class<?>) nestedOrigin.getType();
            assertTrue(Dependency.class.isAssignableFrom(cls));
        }
            break;
        case SOURCE: {
            var nestedOrigin = (ClassRefTypeSignature) nested.get();
            var cls = nestedOrigin.getClassInfo().loadClass();
            assertTrue(Dependency.class.isAssignableFrom(cls));
        }
            break;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Bar {
    }

    private static final class TestContext extends BaseTestContext {
        public TestContext(ExtensionContext context) {
            super(context);
        }

        public Arguments getReflectionArguments() throws NoSuchMethodException {
            var origin = getReflectionOrigin();
            var model = ArraySignatureModel.of(origin, mock(Model.class));

            return Arguments.of(model, ModelKind.REFLECTION, this);
        }

        public AnnotatedArrayType getReflectionOrigin()
                throws NoSuchMethodException {
            return (AnnotatedArrayType) Sample.class.getDeclaredMethod("foo")
                    .getAnnotatedReturnType();
        }

        public Arguments getSourceArguments() {
            var origin = getSourceOrigin();
            var model = ArraySignatureModel.of(origin, mock(Model.class));

            return Arguments.of(model, ModelKind.SOURCE, this);
        }

        public ArrayTypeSignature getSourceOrigin() {
            return (ArrayTypeSignature) getScanResult()
                    .getClassInfo(Sample.class.getName()).getMethodInfo("foo")
                    .getSingleMethod("foo").getTypeSignatureOrTypeDescriptor()
                    .getResultType();
        }
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

            annotation = AnnotationInfoModel.of(annotationOrigin,
                    mock(Model.class));
        }

        @DisplayName("It should access annotations")
        @ParameterizedTest(name = ModelProvider.testName)
        @ArgumentsSource(ModelProvider.class)
        public void should_AccessAnnotations(ArraySignatureModel model,
                ModelKind kind, BaseTestContext context) {
            // Annotation is added to String, not String[]; so we need to
            // extract nested type to get the type annotation
            assertEquals(List.of(annotation),
                    model.getNestedType().getAnnotations());
        }
    }

    @Nested
    @DisplayName("As a SpecializedModel")
    public class AsSpecializedModel {
        @DisplayName("It should have an array specialization")
        @ParameterizedTest(name = ModelProvider.testName)
        @ArgumentsSource(ModelProvider.class)
        public void should_HaveArraySpecialization(ArraySignatureModel model,
                ModelKind kind, BaseTestContext context) {
            new SpecializationChecker(model).apply("isArray", "isNonJDKClass");
        }
    }

    public static class ModelProvider implements ArgumentsProvider {
        public static final String testName = "{1}";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) throws NoSuchMethodException {
            var ctx = new TestContext(context);

            return Stream.of(ctx.getReflectionArguments(),
                    ctx.getSourceArguments());
        }
    }

    private static class Dependency {
        public final String baz = "baz";
    }

    private static class Sample {
        @Bar
        public Dependency[] foo() {
            return new Dependency[] {};
        }
    }
}
