package dev.hilla.parser.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.apache.commons.lang3.function.FailableBiFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.hilla.parser.test.helpers.BaseTestContext;
import dev.hilla.parser.test.helpers.ModelKind;
import dev.hilla.parser.test.helpers.ParserExtension;
import dev.hilla.parser.test.helpers.SpecializationChecker;
import dev.hilla.parser.test.helpers.WithScanResult;
import dev.hilla.parser.utils.Streams;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;

@ExtendWith(ParserExtension.class)
public class MethodInfoModelTests {
    private final CharacteristicsModelProvider.Checker characteristicsChecker = new CharacteristicsModelProvider.Checker();

    @DisplayName("It should detect method characteristics correctly")
    @ParameterizedTest(name = CharacteristicsModelProvider.testName)
    @ArgumentsSource(CharacteristicsModelProvider.class)
    public void should_DetectCharacteristics(MethodInfoModel model,
            Object origin, String[] characteristics, ModelKind kind,
            CharacteristicsModelProvider.Context context, String testName) {
        characteristicsChecker.apply(model, characteristics);
    }

    @DisplayName("It should provide method dependencies")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetDependencies(MethodInfoModel model, Object origin,
            ModelKind kind, ModelProvider.Context context) {
        var expected = Set.of(ClassInfoModel.of(Sample.Dependency.class),
                ClassInfoModel.of(Sample.ParamDependency.class));
        var actual = model.getDependencies();

        assertEquals(expected, actual);
    }

    @DisplayName("It should have the same hashCode for source and reflection models")
    @Test
    public void should_HaveSameHashCodeForSourceAndReflectionModels(
            @WithScanResult ScanResult scanResult)
            throws NoSuchMethodException {
        var reflectionModel = getDefaultReflectionModel();
        var sourceModel = getDefaultSourceModel(scanResult);

        assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
    }

    @DisplayName("It should have the same hashCode for source and reflection models")
    @Test
    public void should_HaveSourceAndReflectionModelsEqual(
            @WithScanResult ScanResult scanResult)
            throws NoSuchMethodException {
        var reflectionModel = getDefaultReflectionModel();
        var sourceModel = getDefaultSourceModel(scanResult);

        assertEquals(reflectionModel, reflectionModel);
        assertEquals(reflectionModel, sourceModel);

        assertEquals(sourceModel, sourceModel);
        assertEquals(sourceModel, reflectionModel);

        assertNotEquals(sourceModel, new Object());
        assertNotEquals(reflectionModel, new Object());
    }

    private MethodInfoModel getDefaultReflectionModel()
            throws NoSuchMethodException {
        return MethodInfoModel.of(Sample.class.getDeclaredMethod("method",
                String.class, Sample.ParamDependency.class));
    }

    private MethodInfoModel getDefaultSourceModel(ScanResult scanResult) {
        var a = scanResult.getClassInfo(Sample.class.getName())
                .getDeclaredMethodInfo("method").getSingleMethod("method");

        var str = a.getTypeDescriptorStr();

        return MethodInfoModel.of(scanResult
                .getClassInfo(Sample.class.getName())
                .getDeclaredMethodInfo("method").getSingleMethod("method"));
    }

    public static class CharacteristicsModelProvider
            implements ArgumentsProvider {
        public static final String testName = "{3} [{5}]";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) {
            var ctx = new Context(context);

            return Streams.combine(ctx.getReflectionArguments(),
                    ctx.getSourceArguments());
        }

        public static final class Checker
                extends SpecializationChecker<MethodInfoModel> {
            private static final List<String> allowedMethods = List.of(
                    "isAbstract", "isBridge", "isFinal", "isNative",
                    "isPrivate", "isProtected", "isPublic", "isStatic",
                    "isStrict", "isSynchronized", "isSynthetic", "isVarArgs");

            public Checker() {
                super(MethodInfoModel.class,
                        Arrays.stream(
                                MethodInfoModel.class.getDeclaredMethods())
                                .filter(method -> allowedMethods
                                        .contains(method.getName())));
            }
        }

        public static final class Context extends BaseTestContext {
            private static final List<Association> associations = List.of(
                    new Association("abstractMethod",
                            new String[] { "isPublic", "isAbstract" }),
                    new Association("finalMethod",
                            new String[] { "isPublic", "isFinal" }),
                    new Association("nativeMethod",
                            new String[] { "isPublic", "isNative" }),
                    new Association("privateMethod",
                            new String[] { "isPrivate" }),
                    new Association("protectedMethod",
                            new String[] { "isProtected" }),
                    new Association("staticMethod",
                            new String[] { "isPublic", "isStatic" }),
                    new Association("synchronizedMethod",
                            new String[] { "isPublic", "isSynchronized" }),
                    new Association("varArgsMethod",
                            new String[] { "isPublic", "isVarArgs" },
                            (cls, name) -> cls.getDeclaredMethod(name,
                                    String[].class),
                            (cls, name) -> cls.getDeclaredMethodInfo(name)
                                    .getSingleMethod(name)));

            Context(ExtensionContext context) {
                super(context);
            }

            public Stream<Arguments> getReflectionArguments() {
                return associations.stream().map(association -> {
                    var origin = association.getOrigin();
                    var model = MethodInfoModel.of(origin);

                    return Arguments.of(model, origin,
                            association.getCharacteristics(),
                            ModelKind.REFLECTION, this, association.getName());
                });
            }

            public Stream<Arguments> getSourceArguments() {
                var scanResult = getScanResult();

                return associations.stream().map(association -> {
                    var origin = association.getOrigin(scanResult);
                    var model = MethodInfoModel.of(origin);

                    return Arguments.of(model, origin,
                            association.getCharacteristics(), ModelKind.SOURCE,
                            this, association.getName());
                });
            }

            private static final class Association {
                private final String[] checkers;
                private final String name;
                private final FailableBiFunction<Class<?>, String, Method, Throwable> reflectionExtractor;
                private final BiFunction<ClassInfo, String, MethodInfo> sourceExtractor;

                private Association(String name, String[] checkers,
                        FailableBiFunction<Class<?>, String, Method, Throwable> reflectionExtractor,
                        BiFunction<ClassInfo, String, MethodInfo> sourceExtractor) {
                    this.checkers = checkers;
                    this.name = name;
                    this.reflectionExtractor = reflectionExtractor;
                    this.sourceExtractor = sourceExtractor;
                }

                private Association(String name, String[] checkers) {
                    this(name, checkers, null, null);
                }

                public Method getOrigin() {
                    try {
                        return reflectionExtractor != null
                                ? reflectionExtractor
                                        .apply(Characteristics.class, name)
                                : Characteristics.class.getDeclaredMethod(name);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }

                public MethodInfo getOrigin(ScanResult scanResult) {
                    var cls = scanResult
                            .getClassInfo(Characteristics.class.getName());

                    return sourceExtractor != null
                            ? sourceExtractor.apply(cls, name)
                            : cls.getDeclaredMethodInfo(name)
                                    .getSingleMethod(name);
                }

                public String[] getCharacteristics() {
                    return checkers;
                }

                public String getName() {
                    return name;
                }
            }
        }
    }

    public static class ModelProvider implements ArgumentsProvider {
        public static final String testName = "{2}";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) throws NoSuchMethodException {
            var ctx = new Context(context);

            return Stream.of(ctx.getReflectionArguments(),
                    ctx.getSourceArguments());
        }

        public static class Context extends BaseTestContext {
            Context(ExtensionContext context) {
                super(context);
            }

            public Arguments getReflectionArguments()
                    throws NoSuchMethodException {
                var origin = Sample.class.getDeclaredMethod("method",
                        String.class, Sample.ParamDependency.class);
                var model = MethodInfoModel.of(origin);

                return Arguments.of(model, origin, ModelKind.REFLECTION, this);
            }

            public Arguments getSourceArguments() {
                var origin = getScanResult()
                        .getClassInfo(Sample.class.getName())
                        .getDeclaredMethodInfo("method")
                        .getSingleMethod("method");
                var model = MethodInfoModel.of(origin);

                return Arguments.of(model, origin, ModelKind.SOURCE, this);
            }
        }
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

    private static class Sample {
        public Dependency method(String first, ParamDependency second) {
            return null;
        }

        private static class Dependency {
        }

        private static class ParamDependency {
        }
    }
}
