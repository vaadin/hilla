package dev.hilla.parser.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.function.Failable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.hilla.parser.test.helpers.BaseTestContext;
import dev.hilla.parser.test.helpers.ParserExtension;
import dev.hilla.parser.utils.Streams;

import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.ScanResult;
import one.util.streamex.StreamEx;

@ExtendWith(ParserExtension.class)
public class ClassRefSignatureModelTests {

//    @DisplayName("It should detect method characteristics correctly")
//    @ParameterizedTest(name = ModelProvider.testNamePattern)
//    @ArgumentsSource(ModelProvider.class)
//    public void should_DetectCharacteristics(ClassRefSignatureModel model,
//            Object origin, String[] characteristics, ModelKind kind,
//            CharacteristicsModelProvider.Context context,
//            String testName) {
//
//    }

    @DisplayName("It should have the same hashCode for source and reflection models")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveSameHashCodeForSourceAndReflectionModels(
            Map.Entry<ClassRefSignatureModel, ClassRefSignatureModel> models,
            ModelProvider.Context context, String testName) {
        var reflectionModel = models.getKey();
        var sourceModel = models.getValue();

        assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
    }

    @DisplayName("It should have source and reflection models equal")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveSourceAndReflectionModelsEqual(
            Map.Entry<ClassRefSignatureModel, ClassRefSignatureModel> models,
            ModelProvider.Context context, String testName) {
        var reflectionModel = models.getKey();
        var sourceModel = models.getValue();

        assertEquals(reflectionModel, reflectionModel);
        assertEquals(reflectionModel, sourceModel);

        assertEquals(sourceModel, sourceModel);
        assertEquals(sourceModel, reflectionModel);

        assertNotEquals(sourceModel, new Object());
        assertNotEquals(reflectionModel, new Object());
    }

    private ClassRefSignatureModel getDefaultReflectionModel()
            throws NoSuchFieldException {
        return ClassRefSignatureModel.of(Sample.class
                .getDeclaredField("dynamicDependency").getAnnotatedType());
    }

    private ClassRefSignatureModel getDefaultSourceModel(
            ScanResult scanResult) {
        return ClassRefSignatureModel.of((ClassRefTypeSignature) scanResult
                .getClassInfo(Sample.class.getName())
                .getDeclaredFieldInfo("dynamicDependency")
                .getTypeSignatureOrTypeDescriptor());
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

    public static final class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) {
            var ctx = new Context(context);
            return Streams.combine(ctx.getCompleteReflectionArguments(),
                    ctx.getBareReflectionArguments(), ctx.getSourceArguments());
        }

        public static final class Context extends BaseTestContext {
            private static final List<String> fields = List.of(
                    "dynamicDependency", "dynamicParametrizedDependency",
                    "staticDependency", "staticParametrizedDependency",
                    "topLevelParametrizedDependency");

            public Context(ExtensionContext context) {
                super(context);
            }

            public Stream<Arguments> getBareReflectionArguments() {
                return fields.stream()
                        .filter(name -> !name.contains("Parametrized"))
                        .map(Failable.asFunction((name) -> {
                            var origin = Sample.class.getDeclaredField(name)
                                    .getType();
                            return Arguments.of(
                                    ClassRefSignatureModel.of(origin), origin,
                                    ModelKind.REFLECTION_BARE, this, name);
                        }));
            }

            public Stream<Arguments> getCompleteReflectionArguments() {
                return fields.stream().map(Failable.asFunction((name) -> {
                    var origin = Sample.class.getDeclaredField(name)
                            .getAnnotatedType();
                    return Arguments.of(ClassRefSignatureModel.of(origin),
                            origin, ModelKind.REFLECTION_COMPLETE, this, name);
                }));
            }

            public Stream<Arguments> getSourceArguments() {
                var cls = getScanResult().getClassInfo(Sample.class.getName());

                return fields.stream().map(name -> {
                    var origin = (ClassRefTypeSignature) cls
                            .getDeclaredFieldInfo(name)
                            .getTypeSignatureOrTypeDescriptor();
                    return Arguments.of(ClassRefSignatureModel.of(origin),
                            origin, ModelKind.SOURCE, this, name);
                });
            }
        }

        public static final class Equality implements ArgumentsProvider {
            public static final String testNamePattern = "BOTH [{2}]";

            private static Arguments zip(Map.Entry<Arguments, Arguments> pair) {
                var reflectionArgs = pair.getKey().get();
                var sourceArgs = pair.getValue().get();

                return Arguments.of(Map.entry(reflectionArgs[0], sourceArgs[0]),
                        reflectionArgs[reflectionArgs.length - 2],
                        reflectionArgs[reflectionArgs.length - 1]);
            }

            @Override
            public Stream<? extends Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context(context);

                return StreamEx.of(ctx.getCompleteReflectionArguments())
                        .zipWith(ctx.getSourceArguments()).map(Equality::zip);
            }
        }

    }

    private static class Sample {
        private @Bar DynamicDependency.@Foo SubDeps dynamicDependency;
        private @Bar DynamicDependencyWithTypeArgs<String>.@Foo SubDeps<Integer> dynamicParametrizedDependency;
        private StaticDependency.@Foo SubDeps staticDependency;
        private StaticDependencyWithTypeArgs.@Foo SubDeps<Integer> staticParametrizedDependency;
        private @Foo List<String> topLevelParametrizedDependency;

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE_USE)
        @interface Bar {
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE_USE)
        @interface Foo {
        }

        class DynamicDependency {
            class SubDeps {
            }
        }

        class DynamicDependencyWithTypeArgs<U> {
            class SubDeps<T> {
            }
        }

        static class StaticDependency {
            static class SubDeps {
            }
        }

        static class StaticDependencyWithTypeArgs<U> {
            static class SubDeps<T> {
            }
        }
    }
}
