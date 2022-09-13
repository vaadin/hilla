package dev.hilla.parser.models;

import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredField;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.function.Failable;
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
import dev.hilla.parser.utils.Streams;

import io.github.classgraph.AnnotationClassRef;
import io.github.classgraph.AnnotationEnumValue;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValue;
import io.github.classgraph.ScanResult;

@ExtendWith(SourceExtension.class)
public class AnnotationInfoModelTests {
    private Context ctx;

    @BeforeEach
    public void setUp(@Source ScanResult source) {
        ctx = new Context(source);
    }

    @DisplayName("It should get class info")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetClassInfo(AnnotationInfoModel model, ModelKind kind) {
        assertEquals(Optional.of(ClassInfoModel.of(Sample.Foo.class)),
                model.getClassInfo());
    }

    @DisplayName("It should get annotations parameters")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetParameters(AnnotationInfoModel model,
            ModelKind kind) {
        var expected = Set.of(
                AnnotationParameterModel.of("stringParameter", "foo1"),
                AnnotationParameterModel.of("intParameter", 10),
                AnnotationParameterModel.of("classParameter", Sample.class),
                AnnotationParameterModel.of("enumParameter",
                        Sample.Enum.VALUE));
        var actual = model.getParameters();
        assertEquals(expected, actual);
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

    @DisplayName("It should provide correct origin")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectOrigin(AnnotationInfoModel model,
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

    static final class Context {
        private static final String fieldName = "bar";
        private static final Enum<?> reflectionEnumValue;
        private static final Sample.Foo reflectionOrigin = getDeclaredField(
                Sample.class, fieldName).getAnnotation(Sample.Foo.class);
        private static final Map<String, Object> reflectionParameterOrigins;

        static {
            reflectionParameterOrigins = getDeclaredMethods(Sample.Foo.class)
                    .collect(Collectors.toMap(Method::getName,
                            Failable.asFunction(method -> method
                                    .invoke(reflectionOrigin))));
            reflectionEnumValue = (Enum<?>) reflectionParameterOrigins
                    .get("enumParameter");
        }

        private final AnnotationEnumValue sourceEnumValue;
        private final AnnotationInfo sourceOrigin;
        private final Map<String, AnnotationParameterValue> sourceParameterOrigins;

        Context(ExtensionContext context) {
            this(SourceExtension.getSource(context));
        }

        Context(ScanResult source) {
            sourceOrigin = getDeclaredField(Sample.class, fieldName, source)
                    .getAnnotationInfo(Sample.Foo.class);
            sourceParameterOrigins = sourceOrigin.getParameterValues().stream()
                    .collect(Collectors.toMap(AnnotationParameterValue::getName,
                            Function.identity()));
            sourceEnumValue = (AnnotationEnumValue) sourceParameterOrigins
                    .get("enumParameter").getValue();
        }

        public Enum<?> getReflectionEnumValue() {
            return reflectionEnumValue;
        }

        public Sample.Foo getReflectionOrigin() {
            return reflectionOrigin;
        }

        public Map<String, Object> getReflectionParameterOrigins() {
            return reflectionParameterOrigins;
        }

        public AnnotationEnumValue getSourceEnumValue() {
            return sourceEnumValue;
        }

        public AnnotationInfo getSourceOrigin() {
            return sourceOrigin;
        }

        public Map<String, AnnotationParameterValue> getSourceParameterOrigins() {
            return sourceParameterOrigins;
        }
    }

    static final class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{1}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
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
        @Foo(stringParameter = "foo1", classParameter = Sample.class, enumParameter = Enum.VALUE)
        private String bar;

        enum Enum {
            VALUE
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.FIELD)
        @interface Foo {
            Class<?> classParameter();

            Enum enumParameter();

            int intParameter()

            default 10;

            String stringParameter() default "bar1";
        }
    }

    @Nested
    @DisplayName("As a NamedModel")
    public class AsNamedModel {
        @DisplayName("It should have name")
        @ParameterizedTest(name = ModelProvider.testNamePattern)
        @ArgumentsSource(ModelProvider.class)
        public void should_HaveName(AnnotationInfoModel model, ModelKind kind) {
            assertEquals(Sample.Foo.class.getName(), model.getName());
        }
    }

    @DisplayName("Annotation enum value parameter")
    @Nested
    public class EnumValue {
        @DisplayName("It should provide all information about value")
        @ParameterizedTest(name = EnumValueProvider.testNamePattern)
        @ArgumentsSource(EnumValueProvider.class)
        public void should_GetClassInfo(AnnotationParameterEnumValueModel model,
                ModelKind kind) {
            assertEquals(ClassInfoModel.of(Sample.Enum.class),
                    model.getClassInfo());
            assertEquals("VALUE", model.getValueName());
        }

        @DisplayName("It should have the same hashCode for source and reflection models")
        @Test
        public void should_HaveSameHashCodeForSourceAndReflectionModels() {
            var reflectionModel = AnnotationParameterEnumValueModel
                    .of(ctx.getReflectionEnumValue());
            var sourceModel = AnnotationParameterEnumValueModel
                    .of(ctx.getSourceEnumValue());

            assertEquals(reflectionModel, reflectionModel);
            assertEquals(reflectionModel, sourceModel);

            assertEquals(sourceModel, sourceModel);
            assertEquals(sourceModel, reflectionModel);

            assertNotEquals(reflectionModel, new Object());
            assertNotEquals(sourceModel, new Object());
        }

        @DisplayName("It should have source and reflection models equal")
        @Test
        public void should_HaveSourceAndReflectionModelsEqual() {
            var reflectionModel = AnnotationParameterEnumValueModel
                    .of(ctx.getReflectionEnumValue());
            var sourceModel = AnnotationParameterEnumValueModel
                    .of(ctx.getSourceEnumValue());

            assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
        }
    }

    @DisplayName("Annotation parameter")
    @Nested
    public class Parameter {
        @DisplayName("It should get name and value correctly")
        @ParameterizedTest(name = ParameterProvider.testNamePattern)
        @ArgumentsSource(ParameterProvider.class)
        public void should_GetNameAndValue(AnnotationParameterModel model,
                ModelKind kind, String name) {
            assertEquals(name, model.getName());
            assertEquals(process(ctx.getReflectionParameterOrigins().get(name)),
                    model.getValue());
        }

        @DisplayName("It should have the same hashCode for source and reflection models")
        @ParameterizedTest(name = EqualityParameterProvider.testNamePattern)
        @ArgumentsSource(EqualityParameterProvider.class)
        public void should_HaveSameHashCodeForSourceAndReflectionModels(
                AnnotationParameterModel reflectionModel,
                AnnotationParameterModel sourceModel, String name) {
            assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
        }

        @DisplayName("It should have source and reflection models equal")
        @ParameterizedTest(name = EqualityParameterProvider.testNamePattern)
        @ArgumentsSource(EqualityParameterProvider.class)
        public void should_HaveSourceAndReflectionModelsEqual(
                AnnotationParameterModel reflectionModel,
                AnnotationParameterModel sourceModel, String name) {
            assertEquals(reflectionModel, reflectionModel);
            assertEquals(reflectionModel, sourceModel);

            assertEquals(sourceModel, sourceModel);
            assertEquals(sourceModel, reflectionModel);

            assertNotEquals(reflectionModel, new Object());
            assertNotEquals(sourceModel, new Object());
        }

        private Object process(Object value) {
            if (value instanceof AnnotationClassRef) {
                return ClassInfoModel
                        .of(((AnnotationClassRef) value).getClassInfo());
            } else if (value instanceof Class<?>) {
                return ClassInfoModel.of((Class<?>) value);
            } else if (value instanceof AnnotationEnumValue) {
                return AnnotationParameterEnumValueModel
                        .of((AnnotationEnumValue) value);
            } else if (value instanceof Enum<?>) {
                return AnnotationParameterEnumValueModel.of((Enum<?>) value);
            } else {
                return value;
            }
        }
    }

    static class EnumValueProvider implements ArgumentsProvider {
        private static final String testNamePattern = "{1}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context(context);

            return Stream.of(
                    Arguments.of(
                            AnnotationParameterEnumValueModel
                                    .of(ctx.getReflectionEnumValue()),
                            ModelKind.REFLECTION),
                    Arguments.of(
                            AnnotationParameterEnumValueModel
                                    .of(ctx.getSourceEnumValue()),
                            ModelKind.SOURCE));
        }
    }

    static class EqualityParameterProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{2}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context(context);

            var reflectionOrigins = ctx.getReflectionParameterOrigins();
            var sourceOrigins = ctx.getSourceParameterOrigins();

            return reflectionOrigins.entrySet().stream()
                    .map(entry -> Arguments.of(
                            AnnotationParameterModel.of(entry),
                            AnnotationParameterModel
                                    .of(sourceOrigins.get(entry.getKey())),
                            entry.getKey()));
        }
    }

    static class ParameterProvider implements ArgumentsProvider {
        private static final String testNamePattern = "{1} [{2}]";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context(context);

            return Streams.combine(
                    ctx.getReflectionParameterOrigins().entrySet().stream()
                            .map(entry -> Arguments.of(
                                    AnnotationParameterModel.of(entry),
                                    ModelKind.REFLECTION, entry.getKey())),
                    ctx.getSourceParameterOrigins().entrySet().stream()
                            .map(entry -> Arguments.of(
                                    AnnotationParameterModel
                                            .of(entry.getValue()),
                                    ModelKind.SOURCE, entry.getKey())));
        }
    }
}
