package dev.hilla.parser.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import io.github.classgraph.ScanResult;
import io.github.classgraph.TypeParameter;

@ExtendWith(SourceExtension.class)
public class TypeParameterModelTests {
    private static final String defaultParameterName = "RegularTypeParameter";
    private Context ctx;

    @BeforeEach
    public void setUp(@Source ScanResult source) {
        this.ctx = new Context(source);
    }

    @DisplayName("It should get bounds")
    @ParameterizedTest(name = ModelProvider.All.testNamePattern)
    @ArgumentsSource(ModelProvider.All.class)
    public void should_GetBounds(TypeParameterModel model, ModelKind kind,
            String name) {
        switch (name) {
        case "RegularTypeParameter":
            assertEquals(List.of(), model.getBounds());
            break;
        case "BoundedTypeParameter":
            assertEquals(List.of(), model.getBounds());
            break;
        }
    }

    @DisplayName("It should have the same hashCode for source and reflection models")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveSameHashCodeForSourceAndReflectionModels(
            TypeParameterModel reflectionModel, TypeParameterModel sourceModel,
            String name) {
        assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
    }

    @DisplayName("It should have source and reflection models equal")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveSourceAndReflectionModelsEqual(
            TypeParameterModel reflectionModel, TypeParameterModel sourceModel,
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
    public void should_ProvideCorrectOrigin(TypeParameterModel model,
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

    static final class Context {
        private static final Map<String, TypeVariable<?>> reflectionOrigins = Arrays
                .stream(Sample.class.getTypeParameters()).collect(Collectors
                        .toMap(TypeVariable::getName, Function.identity()));

        private final ScanResult source;
        private final Map<String, TypeParameter> sourceOrigins;

        Context(ScanResult source) {
            this.source = source;
            this.sourceOrigins = source.getClassInfo(Sample.class.getName())
                    .getTypeSignatureOrTypeDescriptor().getTypeParameters()
                    .stream().collect(Collectors.toMap(TypeParameter::getName,
                            Function.identity()));
        }

        Context(ExtensionContext context) {
            this(SourceExtension.getSource(context));
        }

        public TypeVariable<?> getReflectionOrigin(String name) {
            return reflectionOrigins.get(name);
        }

        public Map<String, TypeVariable<?>> getReflectionOrigins() {
            return reflectionOrigins;
        }

        public ScanResult getSource() {
            return source;
        }

        public TypeParameter getSourceOrigin(String name) {
            return sourceOrigins.get(name);
        }

        public Map<String, TypeParameter> getSourceOrigins() {
            return sourceOrigins;
        }
    }

    static final class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{1}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context(context);

            return Stream.of(
                    Arguments.of(
                            TypeParameterModel.of(ctx
                                    .getReflectionOrigin(defaultParameterName)),
                            ModelKind.REFLECTION),
                    Arguments.of(
                            TypeParameterModel.of(
                                    ctx.getSourceOrigin(defaultParameterName)),
                            ModelKind.SOURCE));
        }

        static final class All implements ArgumentsProvider {
            public static final String testNamePattern = "{2}";

            @Override
            public Stream<Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context(context);

                return Streams.combine(
                        ctx.getReflectionOrigins().entrySet().stream()
                                .map(entry -> Arguments.of(
                                        TypeParameterModel.of(entry.getValue()),
                                        ModelKind.REFLECTION, entry.getKey())),
                        ctx.getSourceOrigins().entrySet().stream()
                                .map(entry -> Arguments.of(
                                        TypeParameterModel.of(entry.getValue()),
                                        ModelKind.SOURCE, entry.getKey())));
            }
        }

        static final class Equality implements ArgumentsProvider {
            public static final String testNamePattern = "{2}";

            @Override
            public Stream<Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context(context);

                return ctx.getReflectionOrigins().entrySet().stream()
                        .map(entry -> Arguments.of(
                                TypeParameterModel.of(entry.getValue()),
                                TypeParameterModel.of(
                                        ctx.getSourceOrigin(entry.getKey())),
                                entry.getKey()));
            }
        }
    }

    static final class Sample<@Sample.Foo RegularTypeParameter, @Sample.Foo BoundedTypeParameter extends Sample.Bound> {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE_USE)
        @interface Foo {
        }

        static class Bound {
        }
    }
}
