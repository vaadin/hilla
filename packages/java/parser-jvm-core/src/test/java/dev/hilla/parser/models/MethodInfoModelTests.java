package dev.hilla.parser.models;

import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethod;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethods;
import static dev.hilla.parser.test.helpers.SpecializationChecker.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
import dev.hilla.parser.utils.Streams;

import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;

@ExtendWith(SourceExtension.class)
public class MethodInfoModelTests {
    private Context.Default ctx;

    @BeforeEach
    public void setUp(@Source ScanResult source) {
        ctx = new Context.Default(source);
    }

    @DisplayName("It should provide method dependencies")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetDependencies(MethodInfoModel model, ModelKind kind) {
        var expected = Set.of(ClassInfoModel.of(Sample.Dependency.class),
                ClassInfoModel.of(Sample.ParamDependency.class));
        var actual = model.getDependencies();

        assertEquals(expected, actual);
    }

    @DisplayName("It should have the same hashCode for source and reflection models")
    @Test
    public void should_HaveSameHashCodeForSourceAndReflectionModels() {
        var reflectionModel = MethodInfoModel.of(ctx.getReflectionOrigin());
        var sourceModel = MethodInfoModel.of(ctx.getSourceOrigin());

        assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
    }

    @DisplayName("It should have source and reflection models equal")
    @Test
    public void should_HaveSourceAndReflectionModelsEqual() {
        var reflectionModel = MethodInfoModel.of(ctx.getReflectionOrigin());
        var sourceModel = MethodInfoModel.of(ctx.getSourceOrigin());

        assertEquals(reflectionModel, reflectionModel);
        assertEquals(reflectionModel, sourceModel);

        assertEquals(sourceModel, sourceModel);
        assertEquals(sourceModel, reflectionModel);

        assertNotEquals(sourceModel, new Object());
        assertNotEquals(reflectionModel, new Object());
    }

    @DisplayName("As a method model with characteristics")
    @Nested
    public class AsCharacterizedMethodModel {
        private final CharacteristicsModelProvider.Checker checker = new CharacteristicsModelProvider.Checker();

        @DisplayName("It should detect method characteristics correctly")
        @ParameterizedTest(name = CharacteristicsModelProvider.testName)
        @ArgumentsSource(CharacteristicsModelProvider.class)
        public void should_DetectCharacteristics(MethodInfoModel model,
                String[] characteristics, ModelKind kind, String testName) {
            checker.apply(model, characteristics);
        }
    }

    public static class CharacteristicsModelProvider
            implements ArgumentsProvider {
        public static final String testName = "{2} [{3}]";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) throws NoSuchMethodException {
            var ctx = new Context.Characteristics(context);

            return Streams.combine(
                    ctx.getReflectionCharacteristics().entrySet().stream()
                            .map(entry -> Arguments.of(
                                    MethodInfoModel.of(entry.getKey()),
                                    entry.getValue(), ModelKind.REFLECTION,
                                    entry.getKey().getName())),
                    ctx.getSourceCharacteristics().entrySet().stream()
                            .map(entry -> Arguments.of(
                                    MethodInfoModel.of(entry.getKey()),
                                    entry.getValue(), ModelKind.SOURCE,
                                    entry.getKey().getName())));
        }

        public static final class Checker
                extends SpecializationChecker<MethodInfoModel> {
            private static final List<String> allowedMethods = List.of(
                    "isAbstract", "isBridge", "isFinal", "isNative",
                    "isPrivate", "isProtected", "isPublic", "isStatic",
                    "isStrict", "isSynchronized", "isSynthetic", "isVarArgs");

            public Checker() {
                super(MethodInfoModel.class,
                        getDeclaredMethods(MethodInfoModel.class),
                        allowedMethods);
            }
        }
    }

    public static class ModelProvider implements ArgumentsProvider {
        public static final String testName = "{1}";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) {
            var ctx = new Context.Default(context);

            return Stream.of(
                    Arguments.of(MethodInfoModel.of(ctx.getReflectionOrigin()),
                            ModelKind.REFLECTION),
                    Arguments.of(MethodInfoModel.of(ctx.getSourceOrigin()),
                            ModelKind.SOURCE));
        }
    }

    static abstract class Context {
        private final ScanResult source;

        Context(ScanResult source) {
            this.source = source;
        }

        private ScanResult getSource() {
            return source;
        }

        static final class Characteristics extends Context {
            private static final Map<Method, String[]> reflectionCharacteristics;

            static {
                var refClass = Sample.Characteristics.class;
                reflectionCharacteristics = Map.ofEntries(
                        entry(getDeclaredMethod(refClass, "abstractMethod"),
                                "isPublic", "isAbstract"),
                        entry(getDeclaredMethod(refClass, "finalMethod"),
                                "isPublic", "isFinal"),
                        entry(getDeclaredMethod(refClass, "nativeMethod"),
                                "isPublic", "isNative"),
                        entry(getDeclaredMethod(refClass, "privateMethod"),
                                "isPrivate"),
                        entry(getDeclaredMethod(refClass, "protectedMethod"),
                                "isProtected"),
                        entry(getDeclaredMethod(refClass, "staticMethod"),
                                "isPublic", "isStatic"),
                        entry(getDeclaredMethod(refClass, "synchronizedMethod"),
                                "isPublic", "isSynchronized"),
                        entry(getDeclaredMethod(refClass, "varArgsMethod",
                                String[].class), "isPublic", "isVarArgs"));
            }

            private final Map<MethodInfo, String[]> sourceCharacteristics;

            Characteristics(ExtensionContext context) {
                this(SourceExtension.getSource(context));
            }

            Characteristics(ScanResult source) {
                super(source);
                sourceCharacteristics = reflectionCharacteristics.entrySet()
                        .stream().collect(
                                Collectors.toMap(
                                        entry -> getDeclaredMethod(
                                                entry.getKey(), source),
                                        Map.Entry::getValue));
            }

            public Map<Method, String[]> getReflectionCharacteristics() {
                return reflectionCharacteristics;
            }

            public Map<MethodInfo, String[]> getSourceCharacteristics() {
                return sourceCharacteristics;
            }
        }

        static final class Default extends Context {
            private static final String methodName = "method";
            private static final Method reflectionOrigin = getDeclaredMethod(
                    Sample.class, methodName, String.class,
                    Sample.ParamDependency.class);;
            private final MethodInfo sourceOrigin;

            Default(ExtensionContext context) {
                this(SourceExtension.getSource(context));
            }

            Default(ScanResult source) {
                super(source);
                sourceOrigin = getDeclaredMethod(Sample.class, methodName,
                        source);
            }

            public Method getReflectionOrigin() {
                return reflectionOrigin;
            }

            public MethodInfo getSourceOrigin() {
                return sourceOrigin;
            }
        }
    }

    private static class Sample {
        public Dependency method(String first, ParamDependency second) {
            return null;
        }

        private static abstract class Characteristics {
            public static String staticMethod() {
                return "";
            }

            public final String finalMethod() {
                return "";
            }

            public abstract String abstractMethod();

            public native boolean nativeMethod();

            public synchronized String synchronizedMethod() {
                return "";
            }

            public String varArgsMethod(String... input) {
                return "";
            }

            protected String protectedMethod() {
                return "";
            }

            private String privateMethod() {
                return "";
            }
        }

        private static class Dependency {
        }

        private static class ParamDependency {
        }
    }
}
