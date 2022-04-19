package dev.hilla.parser.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

import dev.hilla.parser.test.helpers.ParserExtension;
import dev.hilla.parser.test.helpers.SpecializationChecker;
import dev.hilla.parser.utils.StreamUtils;

import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ScanResult;

@ExtendWith(ParserExtension.class)
public class BaseSignatureModelTests {
    @DisplayName("It should create correct model")
    @ParameterizedTest(name = "{3} [{2}]")
    @ArgumentsSource(ModelProvider.class)
    public void should_CreateCorrectModel(BaseSignatureModel model,
            Object origin, String name, ModelOriginType type) {
        assertEquals(origin, model.get());

        switch (type) {
        case SOURCE:
            assertTrue(model.isSource());
            break;
        case REFLECTION_BARE:
        case REFLECTION_COMPLETE:
            assertTrue(model.isReflection());
            break;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Bar {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Selector {
    }

    @Nested
    @DisplayName("As an AnnotatedModel")
    public class AsAnnotatedModel {
        private AnnotationInfoModel annotation;

        @BeforeEach
        public void setUp() throws NoSuchMethodException {
            var annotationOrigin = Sample.class.getMethod("getByte")
                    .getAnnotatedReturnType().getAnnotation(Bar.class);

            annotation = AnnotationInfoModel.of(annotationOrigin,
                    mock(Model.class));
        }

        @DisplayName("It should access annotations")
        @ParameterizedTest(name = "{3} [{2}]")
        @ArgumentsSource(AnnotatedModelProvider.class)
        public void should_AccessAnnotations(BaseSignatureModel model,
                Object origin, String name, ModelOriginType type) {
            if (Objects.equals(name, "Byte")) {
                assertEquals(List.of(annotation), model.getAnnotations());
            } else {
                assertEquals(List.of(), model.getAnnotations());
            }
        }
    }

    @Nested
    @DisplayName("As a SpecializedModel")
    public class AsSpecializedModel {
        private final List<String> integerTypeOwners = List.of("Byte", "Short",
                "Long", "Integer");
        private final List<String> floatTypeOwners = List.of("Float", "Double");

        @DisplayName("It should have an array specialization")
        @ParameterizedTest(name = "{3} [{2}]")
        @ArgumentsSource(ModelProvider.class)
        public void should_HaveBaseSpecialization(BaseSignatureModel model,
                Object origin, String name, ModelOriginType type) {
            var specializations = new ArrayList<>(
                    List.of("isBase", "isJDKClass", "is" + name));

            if (!Objects.equals(name, "Void")) {
                specializations.add("isPrimitive");
            }

            if (integerTypeOwners.contains(name)) {
                specializations.add("hasIntegerType");
            }

            if (floatTypeOwners.contains(name)) {
                specializations.add("hasFloatType");
            }

            new SpecializationChecker(model).apply(specializations);
        }
    }

    public static class ModelProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext extensionContext) {
            return StreamUtils.combine(getReflectionModel(),
                    getBareReflectionModel(), getSourceModel(extensionContext));
        }

        private Stream<Arguments> getReflectionModel() {
            return Arrays.stream(Sample.class.getDeclaredMethods())
                    .map(method -> {
                        var origin = method.getAnnotatedReturnType();
                        var model = BaseSignatureModel.of(origin,
                                mock(Model.class));
                        var name = shorten(method.getName());

                        return Arguments.of(model, origin, name,
                                ModelOriginType.REFLECTION_COMPLETE);
                    });
        }

        private Stream<Arguments> getBareReflectionModel() {
            return Arrays.stream(Sample.class.getDeclaredMethods())
                    .map(method -> {
                        var origin = method.getReturnType();
                        var model = BaseSignatureModel.of(origin,
                                mock(Model.class));
                        var name = shorten(method.getName());

                        return Arguments.of(model, origin, name,
                                ModelOriginType.REFLECTION_BARE);
                    });
        }

        private Stream<Arguments> getSourceModel(ExtensionContext context) {
            var store = context.getStore(ParserExtension.STORE);
            var scanResult = (ScanResult) Objects.requireNonNull(
                    store.get(ParserExtension.Keys.SCAN_RESULT));

            return scanResult.getClassesWithAnnotation(Selector.class).stream()
                    .flatMap(cls -> cls.getMethodInfo().stream())
                    .map(method -> {
                        var origin = (BaseTypeSignature) method
                                .getTypeSignatureOrTypeDescriptor()
                                .getResultType();
                        var model = BaseSignatureModel.of(origin,
                                mock(Model.class));
                        var name = shorten(method.getName());

                        return Arguments.of(model, origin, name,
                                ModelOriginType.SOURCE);
                    });
        }

        private String shorten(String name) {
            return name.substring(3);
        }
    }

    // BareSignatureModel.Bare is not supposed to be annotated, so we can safely ignore it.
    public static class AnnotatedModelProvider extends ModelProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(
            ExtensionContext extensionContext) {
            return super.provideArguments(extensionContext).filter(this::isNonBareReflection);
        }

        private boolean isNonBareReflection(Arguments arguments) {
            return !Objects.equals(arguments.get()[3], ModelOriginType.REFLECTION_BARE);
        }
    }

    private enum ModelOriginType {
        SOURCE("SOURCE"), REFLECTION_COMPLETE(
                "REFLECTION (complete)"), REFLECTION_BARE("REFLECTION (bare)");

        private final String text;

        ModelOriginType(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    @Selector
    private static class Sample {
        public boolean getBoolean() {
            return true;
        }

        @Bar
        public byte getByte() {
            return 0;
        }

        public char getCharacter() {
            return 'a';
        }

        public double getDouble() {
            return 0.0;
        }

        public float getFloat() {
            return 0.0f;
        }

        public int getInteger() {
            return 0;
        }

        public long getLong() {
            return 0;
        }

        public short getShort() {
            return 0;
        }

        public void getVoid() {
        }
    }
}
