package dev.hilla.parser.models;

import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredConstructor;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethods;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getParameter;
import static dev.hilla.parser.test.helpers.SpecializationChecker.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

import io.github.classgraph.MethodParameterInfo;
import io.github.classgraph.ScanResult;

@ExtendWith(SourceExtension.class)
public class MethodParameterInfoModelTests {
    private static final String defaultParameterName = "finalParameter";
    private Context.Default ctx;

    @BeforeEach
    public void setUp(@Source ScanResult source) {
        ctx = new Context.Default(source);
    }

    @DisplayName("It should have the same hashCode for source and reflection models")
    @Test
    public void should_HaveSameHashCodeForSourceAndReflectionModels() {
        var reflectionModel = MethodParameterInfoModel
                .of(ctx.getReflectionOrigin(defaultParameterName));
        var sourceModel = MethodParameterInfoModel
                .of(ctx.getSourceOrigin(defaultParameterName));

        assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
    }

    @DisplayName("It should have source and reflection models equal")
    @Test
    public void should_HaveSourceAndReflectionModelsEqual() {
        var reflectionModel = MethodParameterInfoModel
                .of(ctx.getReflectionOrigin(defaultParameterName));
        var sourceModel = MethodParameterInfoModel
                .of(ctx.getSourceOrigin(defaultParameterName));

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
    public void should_ProvideCorrectOrigin(MethodParameterInfoModel model,
            ModelKind kind) {
        switch (kind) {
        case REFLECTION:
            assertEquals(ctx.getReflectionOrigin(defaultParameterName),
                    model.get());
            assertTrue(model.isReflection());
            break;
        case SOURCE:
            assertEquals(ctx.getSourceOrigin(defaultParameterName),
                    model.get());
            assertTrue(model.isSource());
            break;
        }
    }

    static final class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{1}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context.Default(context);

            return Stream.of(
                    Arguments.of(
                            MethodParameterInfoModel.of(ctx
                                    .getReflectionOrigin(defaultParameterName)),
                            ModelKind.REFLECTION),
                    Arguments.of(
                            MethodParameterInfoModel.of(
                                    ctx.getSourceOrigin(defaultParameterName)),
                            ModelKind.SOURCE));
        }

        static final class Characteristics implements ArgumentsProvider {
            public static final String testNamePattern = "{2} [{3}]";

            @Override
            public Stream<Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context.Characteristics(context);

                return Streams.combine(
                        ctx.getReflectionCharacteristics().entrySet().stream()
                                .map(entry -> Arguments.of(
                                        MethodParameterInfoModel
                                                .of(entry.getKey()),
                                        entry.getValue(), ModelKind.REFLECTION,
                                        entry.getKey().getName())),
                        ctx.getSourceCharacteristics().entrySet().stream()
                                .map(entry -> Arguments.of(
                                        MethodParameterInfoModel
                                                .of(entry.getKey()),
                                        entry.getValue(), ModelKind.SOURCE,
                                        entry.getKey().getName())));
            }

            public static final class Checker
                    extends SpecializationChecker<MethodParameterInfoModel> {
                private static final List<String> allowedMethods = List.of(
                        "isFinal", "isMandated", "isImplicit", "isSynthetic");

                public Checker() {
                    super(MethodParameterInfoModel.class,
                            getDeclaredMethods(MethodParameterInfoModel.class),
                            allowedMethods);
                }
            }
        }
    }

    @DisplayName("As a method parameter model with characteristics")
    @Nested
    public class AsCharacterizedMethodModel {
        private final ModelProvider.Characteristics.Checker checker = new ModelProvider.Characteristics.Checker();

        @DisplayName("It should detect method characteristics correctly")
        @ParameterizedTest(name = ModelProvider.Characteristics.testNamePattern)
        @ArgumentsSource(ModelProvider.Characteristics.class)
        public void should_DetectCharacteristics(MethodParameterInfoModel model,
                String[] characteristics, ModelKind kind, String testName) {
            checker.apply(model, characteristics);
        }
    }

    static class Context {
        private final ScanResult source;

        Context(ScanResult source) {
            this.source = source;
        }

        public ScanResult getSource() {
            return source;
        }

        static class Characteristics extends
                AbstractCharacteristics<Parameter, MethodParameterInfo> {
            private static final Map<Parameter, String[]> reflectionCharacteristics;

            static {
                /*
                 * class Sample { class Dyn { Dyn(final String finalParameter,
                 * int defaultParameter) { } } }
                 *
                 * has the following constructor generated by the compiler:
                 *
                 * class Sample { class Dyn { Dyn(Sample this$0, final String
                 * finalParameter, int defaultParameter) { } } }
                 */
                var dynConstructor = getDeclaredConstructor(Sample.Dyn.class,
                        List.of(Sample.class, String.class, int.class));

                /*
                 * enum Enum { FOO, BAR }
                 *
                 * has the following constructor generated by the compiler:
                 *
                 * final class Enum extends java.lang.Enum<Colors> { ...
                 *
                 * private Enum(String $enum$name, int $enum$ordinal) {
                 * super(name, ordinal); }
                 *
                 * ... }
                 */
                var enumConstructor = getDeclaredConstructor(Sample.Enum.class,
                        List.of(String.class, int.class));

                reflectionCharacteristics = Map.ofEntries(
                        entry(getParameter(dynConstructor, 0), "isMandated",
                                "isImplicit", "isFinal"),
                        entry(getParameter(dynConstructor, 1), "isFinal"),
                        entry(getParameter(dynConstructor, 2)),
                        entry(getParameter(enumConstructor, 0), "isSynthetic"),
                        entry(getParameter(enumConstructor, 1), "isSynthetic"));
            }

            Characteristics(ExtensionContext context) {
                this(SourceExtension.getSource(context));
            }

            Characteristics(ScanResult source) {
                super(source, reflectionCharacteristics,
                        reflectionCharacteristics.entrySet().stream()
                                .collect(
                                        Collectors.toMap(
                                                entry -> getParameter(
                                                        entry.getKey(), source),
                                                Map.Entry::getValue)));
            }
        }

        static class Default
                extends AbstractContext<Parameter, MethodParameterInfo> {
            private static final List<Class<?>> constructorParams = List
                    .of(Sample.class, String.class, int.class);

            private static final Map<String, Parameter> reflectionOrigins = Arrays
                    .stream(getDeclaredConstructor(Sample.Dyn.class,
                            constructorParams).getParameters())
                    .collect(Collectors.toMap(Parameter::getName,
                            Function.identity()));

            Default(ExtensionContext context) {
                this(SourceExtension.getSource(context));
            }

            Default(ScanResult source) {
                super(source, reflectionOrigins, Arrays
                        .stream(getDeclaredConstructor(Sample.Dyn.class,
                                constructorParams, source).getParameterInfo())
                        .collect(Collectors.toMap(MethodParameterInfo::getName,
                                Function.identity())));
            }
        }
    }

    static class Sample {
        enum Enum {
            FOO, BAR
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE_USE)
        @interface Foo {
        }

        class Dyn {
            Dyn(@Foo final String finalParameter, @Foo int regularParameter) {
            }
        }
    }
}
