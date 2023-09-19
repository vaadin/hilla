package dev.hilla.parser.models;

import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredFields;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.Field;
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
import dev.hilla.parser.test.helpers.context.AbstractContext;
import dev.hilla.parser.utils.Streams;

import io.github.classgraph.FieldInfo;
import io.github.classgraph.ScanResult;
import io.github.classgraph.TypeVariableSignature;

@ExtendWith(SourceExtension.class)
public class TypeVariableModelTests {
    private Context ctx;

    @BeforeEach
    public void setUp(@Source ScanResult source) {
        ctx = new Context(source);
    }

    @DisplayName("It should get annotations")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetAnnotations(TypeVariableModel model, ModelKind kind,
            String name) {
        assertEquals(List.of(Sample.Foo.class.getName()),
                model.getAnnotations().stream()
                        .map(AnnotationInfoModel::getName)
                        .collect(Collectors.toList()));
    }

    @DisplayName("It should resolve to type parameters")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetResolveToTypeParameters(TypeVariableModel model,
            ModelKind kind, String name) {
        assertEquals(TypeParameterModel.of(ctx.getParameter(name)),
                model.resolve());
    }

    @DisplayName("It should have the same hashCode for source and reflection models")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveSameHashCodeForSourceAndReflectionModels(
            TypeVariableModel reflectionModel, TypeVariableModel sourceModel,
            String name) {
        assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
    }

    @DisplayName("It should have source and reflection models equal")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveSourceAndReflectionModelsEqual(
            TypeVariableModel reflectionModel, TypeVariableModel sourceModel,
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
    public void should_ProvideCorrectOrigin(TypeVariableModel model,
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

    static final class Context extends
            AbstractContext<AnnotatedTypeVariable, TypeVariableSignature> {
        private static final Map<String, TypeVariable<?>> parameters = Arrays
                .stream(Sample.class.getTypeParameters()).collect(Collectors
                        .toMap(TypeVariable::getName, Function.identity()));
        private static final Map<String, AnnotatedTypeVariable> reflectionOrigins = getDeclaredFields(
                Sample.class).map(Field::getAnnotatedType)
                .map(AnnotatedTypeVariable.class::cast)
                .collect(Collectors.toMap(
                        variable -> ((TypeVariable<?>) variable.getType())
                                .getName(),
                        Function.identity()));

        Context(ScanResult source) {
            super(source, reflectionOrigins,
                    getDeclaredFields(Sample.class, source)
                            .map(FieldInfo::getTypeSignatureOrTypeDescriptor)
                            .map(TypeVariableSignature.class::cast)
                            .collect(Collectors.toMap(
                                    TypeVariableSignature::getName,
                                    Function.identity())));
        }

        Context(ExtensionContext context) {
            this(SourceExtension.getSource(context));
        }

        public TypeVariable<?> getParameter(String name) {
            return parameters.get(name);
        }
    }

    static final class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{1} [{2}]";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context)
                throws Exception {
            var ctx = new Context(context);

            return Streams.combine(
                    ctx.getReflectionOrigins().entrySet().stream()
                            .map(entry -> Arguments.of(
                                    TypeVariableModel.of(entry.getValue()),
                                    ModelKind.REFLECTION, entry.getKey())),
                    ctx.getSourceOrigins().entrySet().stream()
                            .map(entry -> Arguments.of(
                                    TypeVariableModel.of(entry.getValue()),
                                    ModelKind.SOURCE, entry.getKey())));
        }

        static final class Checker
                extends SpecializationChecker<SpecializedModel> {
            Checker() {
                super(SpecializedModel.class,
                        getDeclaredMethods(SpecializedModel.class));
            }
        }

        static final class Equality implements ArgumentsProvider {
            public static final String testNamePattern = "{2}";

            @Override
            public Stream<Arguments> provideArguments(ExtensionContext context)
                    throws Exception {
                var ctx = new Context(context);

                return ctx.getReflectionOrigins().entrySet().stream()
                        .map(entry -> Arguments.of(
                                TypeVariableModel.of(entry.getValue()),
                                TypeVariableModel.of(
                                        ctx.getSourceOrigin(entry.getKey())),
                                entry.getKey()));
            }
        }
    }

    @Nested
    @DisplayName("As a SpecializedModel")
    public class AsSpecializedModel {
        private final ModelProvider.Checker checker = new ModelProvider.Checker();

        @DisplayName("It should have a type argument specialization")
        @ParameterizedTest(name = ModelProvider.testNamePattern)
        @ArgumentsSource(ModelProvider.class)
        public void should_HaveSpecialization(TypeVariableModel model,
                ModelKind kind, String name) {
            checker.apply(model, "isTypeVariable", "isNonJDKClass");
        }
    }

    static class Sample<RegularTypeVariable, BoundedTypeVariable extends Sample.Bound> {
        @Foo
        private BoundedTypeVariable boundedTypeVariableField;
        @Foo
        private RegularTypeVariable regularTypeVariableField;

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE_USE)
        @interface Foo {
        }

        static class Bound {
        }
    }
}
