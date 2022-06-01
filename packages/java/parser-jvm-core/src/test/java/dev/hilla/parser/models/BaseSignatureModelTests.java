package dev.hilla.parser.models;

import static dev.hilla.parser.test.helpers.SpecializationChecker.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

import dev.hilla.parser.test.helpers.BaseTestContext;
import dev.hilla.parser.test.helpers.Source;
import dev.hilla.parser.test.helpers.SourceExtension;
import dev.hilla.parser.test.helpers.SpecializationChecker;
import dev.hilla.parser.utils.Streams;

import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodTypeSignature;
import io.github.classgraph.ScanResult;

@ExtendWith(SourceExtension.class)
public class BaseSignatureModelTests {
    private static final Map<String, String[]> specializations = Map.ofEntries(
            entry(Boolean.TYPE.getName(), "isBase", "isJDKClass", "isBoolean",
                    "isPrimitive"),
            entry(Byte.TYPE.getName(), "isBase", "isJDKClass", "isByte",
                    "isPrimitive", "hasIntegerType"),
            entry(Character.TYPE.getName(), "isBase", "isJDKClass",
                    "isCharacter", "isPrimitive"),
            entry(Double.TYPE.getName(), "isBase", "isJDKClass", "isDouble",
                    "isPrimitive", "hasFloatType"),
            entry(Float.TYPE.getName(), "isBase", "isJDKClass", "isFloat",
                    "isPrimitive", "hasFloatType"),
            entry(Integer.TYPE.getName(), "isBase", "isJDKClass", "isInteger",
                    "isPrimitive", "hasIntegerType"),
            entry(Long.TYPE.getName(), "isBase", "isJDKClass", "isLong",
                    "isPrimitive", "hasIntegerType"),
            entry(Short.TYPE.getName(), "isBase", "isJDKClass", "isShort",
                    "isPrimitive", "hasIntegerType"),
            entry(Void.TYPE.getName(), "isBase", "isJDKClass", "isVoid"));

    @DisplayName("It should create correct model")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_CreateCorrectModel(BaseSignatureModel model,
            Object origin, String[] specializations, ModelKind kind,
            ModelProvider.Context context, String testName) {
        switch (kind) {
        case REFLECTION_BARE:
        case REFLECTION_COMPLETE:
            assertEquals(origin, model.get());
            assertTrue(model.isReflection());
            break;
        case SOURCE:
            assertEquals(origin, model.get());
            assertTrue(model.isSource());
            break;
        }
    }

    @DisplayName("It should provide no dependencies")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideNoDependencies(BaseSignatureModel model,
            Object origin, String[] specializations, ModelKind kind,
            ModelProvider.Context context, String testName) {
        assertEquals(Set.of(), model.getDependencies());
    }

    @DisplayName("It should have the same hashCode for source and reflection models")
    @Test
    public void should_HaveSameHashCodeForSourceAndReflectionModels(
            @Source ScanResult scanResult) throws NoSuchMethodException {
        var reflectionModel = getDefaultReflectionModel();
        var bareReflectionModel = getBareReflectionModel();
        var sourceModel = getDefaultSourceModel(scanResult);

        assertEquals(reflectionModel.hashCode(),
                bareReflectionModel.hashCode());
        assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
        assertEquals(bareReflectionModel.hashCode(), sourceModel.hashCode());
    }

    @DisplayName("It should have source and reflection models equal")
    @Test
    public void should_HaveSourceAndReflectionModelsEqual(
            @Source ScanResult scanResult) throws NoSuchMethodException {
        var reflectionModel = getDefaultReflectionModel();
        var bareReflectionModel = getBareReflectionModel();
        var sourceModel = getDefaultSourceModel(scanResult);

        assertEquals(reflectionModel, reflectionModel);
        assertEquals(reflectionModel, sourceModel);

        assertEquals(sourceModel, sourceModel);
        assertEquals(sourceModel, reflectionModel);

        assertEquals(bareReflectionModel, bareReflectionModel);

        // Bare reflection model doesn't have annotations which are essential
        // for equality check
        assertNotEquals(reflectionModel, bareReflectionModel);
        assertNotEquals(sourceModel, bareReflectionModel);
        assertNotEquals(bareReflectionModel, reflectionModel);
        assertNotEquals(bareReflectionModel, sourceModel);

        assertNotEquals(sourceModel, new Object());
        assertNotEquals(reflectionModel, new Object());
        assertNotEquals(bareReflectionModel, new Object());
    }

    private BaseSignatureModel getBareReflectionModel()
            throws NoSuchMethodException {
        return BaseSignatureModel
                .of(Sample.class.getDeclaredMethod("getByte").getReturnType());
    }

    private BaseSignatureModel getDefaultReflectionModel()
            throws NoSuchMethodException {
        return BaseSignatureModel.of(Sample.class.getDeclaredMethod("getByte")
                .getAnnotatedReturnType());
    }

    private BaseSignatureModel getDefaultSourceModel(ScanResult scanResult) {
        return BaseSignatureModel.of((BaseTypeSignature) scanResult
                .getClassInfo(Sample.class.getName()).getMethodInfo("getByte")
                .getSingleMethod("getByte").getTypeSignatureOrTypeDescriptor()
                .getResultType());
    }

    private enum ModelKind {
        SOURCE("SOURCE"), REFLECTION_COMPLETE(
                "REFLECTION (complete)"), REFLECTION_BARE("REFLECTION (bare)");

        private final String text;

        ModelKind(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    private @interface Bar {
    }

    @Nested
    @DisplayName("As an AnnotatedModel")
    public class AsAnnotatedModel {
        private AnnotationInfoModel annotation;

        @BeforeEach
        public void setUp() throws NoSuchMethodException {
            var annotationOrigin = Sample.class.getMethod("getByte")
                    .getAnnotatedReturnType().getAnnotation(Bar.class);

            annotation = AnnotationInfoModel.of(annotationOrigin);
        }

        @DisplayName("It should access annotations")
        @ParameterizedTest(name = ModelProvider.testName)
        @ArgumentsSource(ModelProvider.Annotated.class)
        public void should_AccessAnnotations(BaseSignatureModel model,
                Object origin, String[] specializations, ModelKind kind,
                ModelProvider.Context context, String testName) {
            if (ModelProvider.Context.is(origin, Byte.TYPE)) {
                assertEquals(List.of(annotation), model.getAnnotations());
            } else {
                assertEquals(List.of(), model.getAnnotations());
            }
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
        public void should_HasSpecialization(BaseSignatureModel model,
                Object origin, String[] specializations, ModelKind kind,
                ModelProvider.Context context, String testName) {
            checker.apply(model, specializations);
        }
    }

    public static class ModelProvider implements ArgumentsProvider {
        public static final String testName = "{3} [{5}]";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) {
            var ctx = new Context(context);

            return Streams.combine(ctx.getCompleteReflectionArguments(),
                    ctx.getBareReflectionArguments(), ctx.getSourceArguments());
        }

        // BareSignatureModel.Bare is not supposed to be annotated, so we can
        // safely ignore it.
        public static final class Annotated extends ModelProvider {
            @Override
            public Stream<? extends Arguments> provideArguments(
                    ExtensionContext context) {
                return super.provideArguments(context)
                        .filter(this::isNonBareReflection);
            }

            private boolean isNonBareReflection(Arguments arguments) {
                return !Objects.equals(arguments.get()[3],
                        ModelKind.REFLECTION_BARE);
            }
        }

        public static final class Context extends BaseTestContext {
            public Context(ExtensionContext context) {
                super(context);
            }

            public static boolean is(Object origin, Class<?> cls) {
                String name;

                if (origin instanceof AnnotatedType) {
                    name = ((AnnotatedType) origin).getType().getTypeName();
                } else if (origin instanceof Class<?>) {
                    name = ((Class<?>) origin).getName();
                } else {
                    name = ((BaseTypeSignature) origin).getType().getName();
                }

                return Objects.equals(name, cls.getName());
            }

            public Stream<Arguments> getBareReflectionArguments() {
                return Arrays.stream(Sample.class.getDeclaredMethods())
                        .map(Method::getReturnType)
                        .map(origin -> Arguments.of(
                                BaseSignatureModel.of(origin), origin,
                                specializations.get(origin.getName()),
                                ModelKind.REFLECTION_BARE, this,
                                origin.getSimpleName()));
            }

            public Stream<Arguments> getCompleteReflectionArguments() {
                return Arrays.stream(Sample.class.getDeclaredMethods())
                        .map(Method::getAnnotatedReturnType)
                        .map(origin -> Arguments.of(
                                BaseSignatureModel.of(origin), origin,
                                specializations
                                        .get(origin.getType().getTypeName()),
                                ModelKind.REFLECTION_COMPLETE, this,
                                ((Class<?>) origin.getType()).getSimpleName()));
            }

            public Stream<Arguments> getSourceArguments() {
                return getScanResult().getClassInfo(Sample.class.getName())
                        .getDeclaredMethodInfo().stream()
                        .map(MethodInfo::getTypeSignatureOrTypeDescriptor)
                        .map(MethodTypeSignature::getResultType)
                        .map(BaseTypeSignature.class::cast)
                        .map(origin -> Arguments.of(
                                BaseSignatureModel.of(origin), origin,
                                specializations.get(origin.getType().getName()),
                                ModelKind.SOURCE, this,
                                origin.getType().getName()));
            }
        }
    }

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
