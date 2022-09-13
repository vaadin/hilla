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
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    @DisplayName("It should have the same hashCode for source and reflection models")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveSameHashCodeForSourceAndReflectionModels(
            MethodInfoModel reflectionModel, MethodInfoModel sourceModel,
            String name) {
        assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
    }

    @DisplayName("It should have source and reflection models equal")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveSourceAndReflectionModelsEqual(
            MethodInfoModel reflectionModel, MethodInfoModel sourceModel,
            String name) {
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
            ModelKind kind, String name) {
        switch (kind) {
        case REFLECTION:
            assertEquals(ctx.getReflectionOrigin(name), model.get());
            assertTrue(model.isReflection());
            break;
        case SOURCE:
            assertEquals(ctx.getSourceOrigin(name), model.get());
            assertTrue(model.isSource());
            break;
        }
    }

    @DisplayName("It should provide correct type parameters")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectTypeParameters(MethodInfoModel model,
            ModelKind kind, String name) {
        switch (name) {
        case Context.Default.basicMethodName:
            assertEquals(List.of(), model.getTypeParameters());
            break;
        case Context.Default.genericMethodName:
            assertEquals(ctx.getTypeParameterOrigins().stream()
                    .map(TypeParameterModel::of).collect(Collectors.toList()),
                    model.getTypeParameters());
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
            public static final String basicMethodName = "method";
            public static final String genericMethodName = "genericMethod";
            private static final Map<String, Method> reflectionOrigins = getDeclaredMethods(
                    Sample.class)
                            .collect(Collectors.toMap(Method::getName,
                                    Function.identity()));

            private static final List<TypeVariable<?>> typeParameterOrigins;

            static {
                typeParameterOrigins = Arrays.asList(reflectionOrigins
                        .get(genericMethodName).getTypeParameters());
            }

            Default(ExtensionContext context) {
                this(SourceExtension.getSource(context));
            }

            Default(ScanResult source) {
                super(source, reflectionOrigins,
                        getDeclaredMethods(Sample.class, source)
                                .collect(Collectors.toMap(MethodInfo::getName,
                                        Function.identity())));
            }

            public List<TypeVariable<?>> getTypeParameterOrigins() {
                return typeParameterOrigins;
            }
        }
    }

    static class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{1} [{2}]";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context.Default(context);

            return Streams.combine(
                    ctx.getReflectionOrigins().entrySet().stream()
                            .map(entry -> Arguments.of(
                                    MethodInfoModel.of(entry.getValue()),
                                    ModelKind.REFLECTION, entry.getKey())),
                    ctx.getSourceOrigins().entrySet().stream()
                            .map(entry -> Arguments.of(
                                    MethodInfoModel.of(entry.getValue()),
                                    ModelKind.SOURCE, entry.getKey())));
        }

        static class Characteristics implements ArgumentsProvider {
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

            static final class Checker
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

        static class Equality implements ArgumentsProvider {
            public static final String testNamePattern = "{1} [{2}]";

            @Override
            public Stream<Arguments> provideArguments(ExtensionContext context)
                    throws Exception {
                var ctx = new Context.Default(context);

                return ctx.getReflectionOrigins().entrySet().stream()
                        .map(entry -> Arguments.of(
                                MethodInfoModel.of(entry.getValue()),
                                MethodInfoModel.of(
                                        ctx.getSourceOrigin(entry.getKey())),
                                entry.getKey()));
            }
        }
    }

    static class Sample {
        public <T> T genericMethod(T parameter) {
            return parameter;
        }

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
