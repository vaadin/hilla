package dev.hilla.parser.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
import dev.hilla.parser.test.helpers.ParserExtension;
import dev.hilla.parser.test.helpers.SpecializationChecker;
import dev.hilla.parser.utils.Streams;

import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassInfo;

@ExtendWith(ParserExtension.class)
public class BaseSignatureModelTests {
    @DisplayName("It should create correct model")
    @ParameterizedTest(name = "{2} [{1}]")
    @ArgumentsSource(ModelProvider.class)
    public void should_CreateCorrectModel(BaseSignatureModel model, String name,
            ModelKind kind, TestContext context) throws NoSuchMethodException {
        switch (kind) {
        case REFLECTION_BARE: {
            var origin = context.getBareReflectionOrigin("get" + name);
            assertEquals(origin, model.get());
            assertTrue(model.isReflection());
        }
            break;
        case REFLECTION_COMPLETE: {
            var origin = context.getCompleteReflectionOrigin("get" + name);
            assertEquals(origin, model.get());
            assertTrue(model.isReflection());
        }
            break;
        case SOURCE: {
            var origin = context.getSourceOrigin("get" + name);
            assertEquals(origin, model.get());
            assertTrue(model.isSource());
        }
            break;
        }
    }

    @DisplayName("It should create correct model")
    @ParameterizedTest(name = "{2} [{1}]")
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideNoDependencies(BaseSignatureModel model,
            String name, ModelKind kind, TestContext context) {
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
    @interface Bar {
    }

    private static final class TestContext extends BaseTestContext {
        public TestContext(ExtensionContext context) {
            super(context);
        }

        private static String shorten(String name) {
            return name.substring(3);
        }

        public Stream<Arguments> getBareReflectionArguments() {
            return getReflectionMethodsStream().map(method -> {
                var origin = method.getReturnType();
                var model = BaseSignatureModel.of(origin, mock(Model.class));
                var name = shorten(method.getName());

                return Arguments.of(model, name, ModelKind.REFLECTION_BARE,
                        this);
            });
        }

        public Class<?> getBareReflectionOrigin(String methodName)
                throws NoSuchMethodException {
            return Sample.class.getDeclaredMethod(methodName).getReturnType();
        }

        public Stream<Arguments> getCompleteReflectionArguments() {
            return getReflectionMethodsStream().map(method -> {
                var origin = method.getAnnotatedReturnType();
                var model = BaseSignatureModel.of(origin, mock(Model.class));
                var name = shorten(method.getName());

                return Arguments.of(model, name, ModelKind.REFLECTION_COMPLETE,
                        this);
            });
        }

        public AnnotatedType getCompleteReflectionOrigin(String methodName)
                throws NoSuchMethodException {
            return Sample.class.getDeclaredMethod(methodName)
                    .getAnnotatedReturnType();
        }

        public Stream<Arguments> getSourceArguments() {
            return getSourceClass().getMethodInfo().stream().map(method -> {
                var origin = (BaseTypeSignature) method
                        .getTypeSignatureOrTypeDescriptor().getResultType();
                var model = BaseSignatureModel.of(origin, mock(Model.class));
                var name = shorten(method.getName());

                return Arguments.of(model, name, ModelKind.SOURCE, this);
            });
        }

        public BaseTypeSignature getSourceOrigin(String methodName) {
            return (BaseTypeSignature) getSourceClass()
                    .getMethodInfo(methodName).getSingleMethod(methodName)
                    .getTypeSignatureOrTypeDescriptor().getResultType();
        }

        private Stream<Method> getReflectionMethodsStream() {
            return Arrays.stream(Sample.class.getDeclaredMethods());
        }

        private ClassInfo getSourceClass() {
            return getScanResult().getClassInfo(Sample.class.getName());
        }
    }

    // BareSignatureModel.Bare is not supposed to be annotated, so we can safely
    // ignore it.
    public static class AnnotatedModelProvider extends ModelProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) {
            return super.provideArguments(context)
                    .filter(this::isNonBareReflection);
        }

        private boolean isNonBareReflection(Arguments arguments) {
            return !Objects.equals(arguments.get()[2],
                    ModelKind.REFLECTION_BARE);
        }
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
        @ParameterizedTest(name = "{2} [{1}]")
        @ArgumentsSource(AnnotatedModelProvider.class)
        public void should_AccessAnnotations(BaseSignatureModel model,
                String name, ModelKind kind, TestContext context) {
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
        private final List<String> floatTypeOwners = List.of("Float", "Double");
        private final List<String> integerTypeOwners = List.of("Byte", "Short",
                "Long", "Integer");

        @DisplayName("It should have an array specialization")
        @ParameterizedTest(name = "{2} [{1}]")
        @ArgumentsSource(ModelProvider.class)
        public void should_HaveBaseSpecialization(BaseSignatureModel model,
                String name, ModelKind kind, TestContext context) {
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
                ExtensionContext context) {
            var ctx = new TestContext(context);

            return Streams.combine(ctx.getCompleteReflectionArguments(),
                    ctx.getBareReflectionArguments(), ctx.getSourceArguments());
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
