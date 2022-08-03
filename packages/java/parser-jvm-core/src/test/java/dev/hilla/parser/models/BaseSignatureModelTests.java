package dev.hilla.parser.models;

import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethod;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethods;
import static dev.hilla.parser.test.helpers.SpecializationChecker.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

import dev.hilla.parser.test.helpers.Source;
import dev.hilla.parser.test.helpers.SourceExtension;
import dev.hilla.parser.test.helpers.SpecializationChecker;
import dev.hilla.parser.utils.Streams;

import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;

@ExtendWith(SourceExtension.class)
public class BaseSignatureModelTests {
    private Context ctx;

    @BeforeEach
    public void setUp(@Source ScanResult source) {
        ctx = new Context(source);
    }

    @DisplayName("It should have the same hashCode for source and reflection models")
    @Test
    public void should_HaveSameHashCodeForSourceAndReflectionModels() {
        var methodName = "getByte";
        var reflectionModel = BaseSignatureModel
                .of(ctx.getCompleteReflectionOrigin(methodName));
        var bareReflectionModel = BaseSignatureModel
                .of(ctx.getBareReflectionOrigin(methodName));
        var sourceModel = BaseSignatureModel
                .of(ctx.getSourceOrigin(methodName));

        assertEquals(reflectionModel.hashCode(),
                bareReflectionModel.hashCode());
        assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
        assertEquals(bareReflectionModel.hashCode(), sourceModel.hashCode());
    }

    @DisplayName("It should have source and reflection models equal")
    @Test
    public void should_HaveSourceAndReflectionModelsEqual() {
        var methodName = "getByte";
        var reflectionModel = BaseSignatureModel
                .of(ctx.getCompleteReflectionOrigin(methodName));
        var bareReflectionModel = BaseSignatureModel
                .of(ctx.getBareReflectionOrigin(methodName));
        var sourceModel = BaseSignatureModel
                .of(ctx.getSourceOrigin(methodName));

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

    @DisplayName("It should provide correct origin")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectOrigin(BaseSignatureModel model,
            ModelKind kind, String methodName) {
        switch (kind) {
        case REFLECTION_BARE:
            assertEquals(ctx.getBareReflectionOrigin(methodName), model.get());
            assertTrue(model.isReflection());
            break;
        case REFLECTION_COMPLETE:
            assertEquals(ctx.getCompleteReflectionOrigin(methodName),
                    model.get());
            assertTrue(model.isReflection());
            break;
        case SOURCE:
            assertEquals(ctx.getSourceOrigin(methodName), model.get());
            assertTrue(model.isSource());
            break;
        }
    }

    @DisplayName("It should provide no dependencies")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideNoDependencies(BaseSignatureModel model,
            ModelKind kind, String methodName) {
        assertEquals(Set.of(), model.getDependencies());
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

    static final class Context {
        private static final Annotation annotation = getDeclaredMethod(
                Sample.class, "getByte").getAnnotatedReturnType()
                        .getAnnotation(Bar.class);
        private static final Map<String, Class<?>> bareReflectionOrigins = getDeclaredMethods(
                Sample.class)
                        .collect(Collectors.toMap(Method::getName,
                                Method::getReturnType));
        private static final Map<String, AnnotatedType> completeReflectionOrigins = getDeclaredMethods(
                Sample.class)
                        .collect(Collectors.toMap(Method::getName,
                                Method::getAnnotatedReturnType));
        private final Map<String, BaseTypeSignature> sourceOrigins;

        Context(ExtensionContext context) {
            this(SourceExtension.getSource(context));
        }

        Context(ScanResult source) {
            sourceOrigins = getDeclaredMethods(Sample.class, source)
                    .collect(Collectors.toMap(MethodInfo::getName,
                            method -> (BaseTypeSignature) method
                                    .getTypeSignatureOrTypeDescriptor()
                                    .getResultType()));
        }

        public Annotation getAnnotation() {
            return annotation;
        }

        public Class<?> getBareReflectionOrigin(String name) {
            return bareReflectionOrigins.get(name);
        }

        public Map<String, Class<?>> getBareReflectionOrigins() {
            return bareReflectionOrigins;
        }

        public AnnotatedType getCompleteReflectionOrigin(String name) {
            return completeReflectionOrigins.get(name);
        }

        public Map<String, AnnotatedType> getCompleteReflectionOrigins() {
            return completeReflectionOrigins;
        }

        public BaseTypeSignature getSourceOrigin(String name) {
            return sourceOrigins.get(name);
        }

        public Map<String, BaseTypeSignature> getSourceOrigins() {
            return sourceOrigins;
        }
    }

    static final class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{1} [{2}]";

        static Stream<Arguments> getBareReflectionArguments(Context ctx) {
            return ctx.getBareReflectionOrigins().entrySet().stream()
                    .map(entry -> Arguments.of(
                            BaseSignatureModel.of(entry.getValue()),
                            ModelKind.REFLECTION_BARE, entry.getKey()));
        }

        static Stream<Arguments> getCompleteReflectionArguments(Context ctx) {
            return ctx.getCompleteReflectionOrigins().entrySet().stream()
                    .map(entry -> Arguments.of(
                            BaseSignatureModel.of(entry.getValue()),
                            ModelKind.REFLECTION_COMPLETE, entry.getKey()));
        }

        static Stream<Arguments> getSourceArguments(Context ctx) {
            return ctx.getSourceOrigins().entrySet().stream()
                    .map(entry -> Arguments.of(
                            BaseSignatureModel.of(entry.getValue()),
                            ModelKind.SOURCE, entry.getKey()));
        }

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context(context);

            return Streams.combine(getCompleteReflectionArguments(ctx),
                    getBareReflectionArguments(ctx), getSourceArguments(ctx));
        }

        // BareSignatureModel.Bare is not supposed to be annotated, so we can
        // safely ignore it.
        static final class Annotated implements ArgumentsProvider {
            public static final String testNamePattern = ModelProvider.testNamePattern;

            @Override
            public Stream<Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context(context);

                return Streams.combine(getCompleteReflectionArguments(ctx),
                        getSourceArguments(ctx));
            }
        }

        static class Checker extends SpecializationChecker<SpecializedModel> {
            Checker() {
                super(SpecializedModel.class,
                        getDeclaredMethods(SpecializedModel.class));
            }
        }

        static class Specialized implements ArgumentsProvider {
            public static final String testNamePattern = "{2} [{3}]";
            private static final Map<Class<?>, String[]> specializations = Map
                    .ofEntries(
                            entry(Boolean.TYPE, "isBase", "isJDKClass",
                                    "isBoolean", "isPrimitive"),
                            entry(Byte.TYPE, "isBase", "isJDKClass", "isByte",
                                    "isPrimitive", "hasIntegerType"),
                            entry(Character.TYPE, "isBase", "isJDKClass",
                                    "isCharacter", "isPrimitive"),
                            entry(Double.TYPE, "isBase", "isJDKClass",
                                    "isDouble", "isPrimitive", "hasFloatType"),
                            entry(Float.TYPE, "isBase", "isJDKClass", "isFloat",
                                    "isPrimitive", "hasFloatType"),
                            entry(Integer.TYPE, "isBase", "isJDKClass",
                                    "isInteger", "isPrimitive",
                                    "hasIntegerType"),
                            entry(Long.TYPE, "isBase", "isJDKClass", "isLong",
                                    "isPrimitive", "hasIntegerType"),
                            entry(Short.TYPE, "isBase", "isJDKClass", "isShort",
                                    "isPrimitive", "hasIntegerType"),
                            entry(Void.TYPE, "isBase", "isJDKClass", "isVoid"));
            private static final Function<Arguments, Arguments> failableInsert = Failable
                    .asFunction(Specialized::insert);

            private static Arguments insert(Arguments args) {
                var list = new ArrayList<>(Arrays.asList(args.get()));
                var methodName = (String) list.get(list.size() - 1);
                var type = getDeclaredMethod(Sample.class, methodName)
                        .getReturnType();

                list.add(1, specializations.get(type));

                return Arguments.of(list.toArray());
            }

            @Override
            public Stream<Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context(context);

                return Streams.combine(
                        getCompleteReflectionArguments(ctx).map(failableInsert),
                        getBareReflectionArguments(ctx).map(failableInsert),
                        getSourceArguments(ctx).map(failableInsert));
            }
        }
    }

    @Nested
    @DisplayName("As an AnnotatedModel")
    public class AsAnnotatedModel {
        @DisplayName("It should access annotations")
        @ParameterizedTest(name = ModelProvider.Annotated.testNamePattern)
        @ArgumentsSource(ModelProvider.Annotated.class)
        public void should_AccessAnnotations(BaseSignatureModel model,
                ModelKind kind, String methodName) {
            if (methodName.equals("getByte")) {
                assertEquals(
                        List.of(AnnotationInfoModel.of(ctx.getAnnotation())),
                        model.getAnnotations());
            } else {
                assertEquals(List.of(), model.getAnnotations());
            }
        }
    }

    @Nested
    @DisplayName("As a SpecializedModel")
    public class AsSpecializedModel {
        private final ModelProvider.Checker checker = new ModelProvider.Checker();

        @DisplayName("It should have an array specialization")
        @ParameterizedTest(name = ModelProvider.Specialized.testNamePattern)
        @ArgumentsSource(ModelProvider.Specialized.class)
        public void should_HaveSpecialization(BaseSignatureModel model,
                String[] specializations, ModelKind kind, String methodName) {
            checker.apply(model, specializations);
        }
    }

    static class Sample {
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
