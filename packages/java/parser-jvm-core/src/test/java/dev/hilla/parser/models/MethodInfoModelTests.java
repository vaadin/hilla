package dev.hilla.parser.models;

import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredConstructor;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethod;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethods;
import static dev.hilla.parser.test.helpers.SpecializationChecker.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
import dev.hilla.parser.test.helpers.context.AbstractCharacteristics;
import dev.hilla.parser.test.helpers.context.AbstractContext;
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
    @ParameterizedTest(name = ModelProvider.testNamePattern)
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

    @DisplayName("It should provide correct origin")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectOrigin(MethodInfoModel model,
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

    @DisplayName("As a method model with characteristics")
    @Nested
    public class AsCharacterizedMethodModel {
        private final ModelProvider.Characteristics.Checker checker = new ModelProvider.Characteristics.Checker();

        @DisplayName("It should detect method characteristics correctly")
        @ParameterizedTest(name = ModelProvider.Characteristics.testNamePattern)
        @ArgumentsSource(ModelProvider.Characteristics.class)
        public void should_DetectCharacteristics(MethodInfoModel model,
                String[] characteristics, ModelKind kind, String testName) {
            checker.apply(model, characteristics);
        }
    }

    public static class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{1}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context.Default(context);

            return Stream.of(
                    Arguments.of(MethodInfoModel.of(ctx.getReflectionOrigin()),
                            ModelKind.REFLECTION),
                    Arguments.of(MethodInfoModel.of(ctx.getSourceOrigin()),
                            ModelKind.SOURCE));
        }

        public static class Characteristics implements ArgumentsProvider {
            public static final String testNamePattern = "{2} [{3}]";

            @Override
            public Stream<Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context.Characteristics(context);

                return Streams.combine(ctx.getReflectionCharacteristics()
                        .entrySet().stream()
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
                        "isAbstract", "isBridge", "isConstructor", "isFinal",
                        "isNative", "isPrivate", "isProtected", "isPublic",
                        "isStatic", "isStrict", "isSynchronized", "isSynthetic",
                        "isVarArgs");

                public Checker() {
                    super(MethodInfoModel.class,
                            getDeclaredMethods(MethodInfoModel.class),
                            allowedMethods);
                }
            }
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

        static final class Characteristics
                extends AbstractCharacteristics<Executable, MethodInfo> {
            private static final Map<Executable, String[]> reflectionCharacteristics;

            static {
                var refClass = Sample.Characteristics.class;

                var bridgeMethod = getDeclaredMethod(
                        Sample.Characteristics.Bridge.class, "compareTo",
                        List.of(Object.class));

                reflectionCharacteristics = Map.ofEntries(
                        entry(getDeclaredConstructor(refClass), "isConstructor",
                                "isPublic"),
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
                                List.of(String[].class)), "isPublic",
                                "isVarArgs"),
                        entry(bridgeMethod, "isPublic", "isBridge",
                                "isSynthetic"));
            }

            Characteristics(ExtensionContext context) {
                this(SourceExtension.getSource(context));
            }

            Characteristics(ScanResult source) {
                super(source, reflectionCharacteristics,
                        reflectionCharacteristics.entrySet().stream()
                                .collect(Collectors.toMap(
                                        entry -> getDeclaredMethod(
                                                entry.getKey(), source),
                                        Map.Entry::getValue)));
            }
        }

        static final class Default extends AbstractContext<Method, MethodInfo> {
            private static final String methodName = "method";
            private static final Map<String, Method> reflectionOrigins = Arrays
                    .stream(Sample.class.getDeclaredMethods())
                    .collect(Collectors.toMap(Method::getName,
                            Function.identity()));

            Default(ExtensionContext context) {
                this(SourceExtension.getSource(context));
            }

            Default(ScanResult source) {
                super(source, reflectionOrigins,
                        source.getClassInfo(Sample.class.getName())
                                .getDeclaredMethodInfo().stream()
                                .collect(Collectors.toMap(MethodInfo::getName,
                                        Function.identity())));
            }

            public Method getReflectionOrigin() {
                return getReflectionOrigin(methodName);
            }

            public MethodInfo getSourceOrigin() {
                return getSourceOrigin(methodName);
            }
        }
    }

    static class Sample {
        public Dependency method(String first, ParamDependency second) {
            return null;
        }

        static abstract class Characteristics {
            public Characteristics() {
            }

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

            static class Bridge implements Comparable<Bridge> {
                @Override
                public int compareTo(Bridge o) {
                    return 0;
                }
            }
        }

        static class Dependency {
        }

        static class ParamDependency {
        }
    }
}
