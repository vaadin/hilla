package dev.hilla.parser.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

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

import dev.hilla.parser.test.helpers.ModelOriginType;
import dev.hilla.parser.test.helpers.ParserExtension;

@ExtendWith(ParserExtension.class)
public class AnnotationInfoModelTests {
    @DisplayName("It should create correct model")
    @ParameterizedTest(name = "{2}")
    @ArgumentsSource(ModelProvider.class)
    public void should_CreateCorrectModel(AnnotationInfoModel model,
            Object origin, ModelOriginType type) {
        assertEquals(origin, model.get());

        switch (type) {
        case SOURCE:
            assertTrue(model.isSource());
            break;
        case REFLECTION:
            assertTrue(model.isReflection());
            break;
        }
    }

    @DisplayName("It should provide no dependencies")
    @ParameterizedTest(name = "{2}")
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideNoDependencies(AnnotationInfoModel model,
            Object origin, ModelOriginType type) {
        assertEquals(0, model.getDependencies().size());
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Foo {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Selector {
    }

    @Nested
    @DisplayName("As a NamedModel")
    public class AsNamedModel {
        @DisplayName("It should have name")
        @ParameterizedTest(name = "{2}")
        @ArgumentsSource(ModelProvider.class)
        public void should_HaveName(AnnotationInfoModel model, Object origin,
                ModelOriginType type) {
            assertEquals(Foo.class.getName(), model.getName());
        }
    }

    public static class ModelProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) throws NoSuchMethodException {
            return Stream.of(getReflectionModel(), getSourceModel(context));
        }

        private Arguments getReflectionModel() throws NoSuchMethodException {
            var origin = Sample.class.getMethod("bar").getAnnotation(Foo.class);
            var model = AnnotationInfoModel.of(origin, mock(Model.class));

            return Arguments.of(model, origin, ModelOriginType.REFLECTION);
        }

        private Arguments getSourceModel(ExtensionContext context) {
            var origin = ParserExtension.getScanResult(context)
                    .getClassesWithAnnotation(Selector.class).stream()
                    .flatMap(cls -> cls.getMethodInfo().stream())
                    .flatMap(method -> method.getAnnotationInfo().stream())
                    .findFirst().get();
            var model = AnnotationInfoModel.of(origin, mock(Model.class));

            return Arguments.of(model, origin, ModelOriginType.SOURCE);
        }
    }

    @Selector
    private static class Sample {
        @Foo
        public void bar() {
        }
    }
}
