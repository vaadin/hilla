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

import dev.hilla.parser.test.helpers.ModelOriginType;
import dev.hilla.parser.test.helpers.ParserExtension;
import dev.hilla.parser.test.helpers.SpecializationChecker;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.ScanResult;

@ExtendWith(ParserExtension.class)
public class ArraySignatureModelTests {
    @DisplayName("It should create correct model")
    @ParameterizedTest(name = "{2}")
    @ArgumentsSource(ModelProvider.class)
    public void should_CreateCorrectModel(ArraySignatureModel model,
            Object origin, ModelOriginType type) {
        assertEquals(origin, model.get());

        switch (type) {
        case REFLECTION:
            assertTrue(model.isReflection());
            break;
        case SOURCE:
            assertTrue(model.isSource());
            break;
        }
    }

    @DisplayName("It should provide nested type")
    @ParameterizedTest(name = "{2}")
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideNestedType(ArraySignatureModel model,
            Object origin, ModelOriginType type) {
        var nested = model.getNestedType();
        assertTrue(nested instanceof ClassRefSignatureModel);

        switch (type) {
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

    @DisplayName("It should provide dependencies")
    @ParameterizedTest(name = "{2}")
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideDependencies(ArraySignatureModel model,
            Object origin, ModelOriginType type) {
        var dependencies = model.getDependencies();

        assertEquals(
                Set.of(ClassInfoModel.of(Dependency.class, mock(Model.class))),
                dependencies);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Bar {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    private @interface Selector {
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
        @ParameterizedTest(name = "{2}")
        @ArgumentsSource(ModelProvider.class)
        public void should_AccessAnnotations(ArraySignatureModel model,
                Object origin, ModelOriginType type) {
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
        @ParameterizedTest(name = "{2}")
        @ArgumentsSource(ModelProvider.class)
        public void should_HaveArraySpecialization(ArraySignatureModel model,
                Object origin, ModelOriginType type) {
            new SpecializationChecker(model).apply("isArray", "isNonJDKClass");
        }
    }

    public static class ModelProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext extensionContext) throws Exception {
            return Stream.of(getReflectionModel(),
                    getSourceModel(extensionContext));
        }

        private Arguments getReflectionModel() throws NoSuchMethodException {
            var origin = (AnnotatedArrayType) Sample.class.getMethod("foo")
                    .getAnnotatedReturnType();

            var model = ArraySignatureModel.of(origin, mock(Model.class));

            return Arguments.of(model, origin, ModelOriginType.REFLECTION);
        }

        private Arguments getSourceModel(ExtensionContext context) {
            var store = context.getStore(ParserExtension.STORE);
            var scanResult = (ScanResult) store
                    .get(ParserExtension.Keys.SCAN_RESULT);
            var origin = (ArrayTypeSignature) scanResult
                    .getClassesWithAnnotation(Selector.class).stream()
                    .flatMap(cls -> cls.getMethodInfo().stream())
                    .map(method -> method.getTypeSignatureOrTypeDescriptor()
                            .getResultType())
                    .findFirst().get();

            var model = ArraySignatureModel.of(origin, mock(Model.class));

            return Arguments.of(model, origin, ModelOriginType.SOURCE);
        }
    }

    private static class Dependency {
        public final String baz = "baz";
    }

    @Selector
    private static class Sample {
        @Bar
        public Dependency[] foo() {
            return new Dependency[] {};
        }
    }
}
