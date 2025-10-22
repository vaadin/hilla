package com.vaadin.hilla.parser.models;

import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredFields;
import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
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

import com.vaadin.hilla.parser.test.helpers.ModelKind;
import com.vaadin.hilla.parser.test.helpers.Source;
import com.vaadin.hilla.parser.test.helpers.SourceExtension;
import com.vaadin.hilla.parser.test.helpers.SpecializationChecker;
import com.vaadin.hilla.parser.test.helpers.context.AbstractContext;
import com.vaadin.hilla.parser.utils.Streams;

import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.ScanResult;
import io.github.classgraph.TypeArgument;

@ExtendWith(SourceExtension.class)
public class TypeArgumentModelTests {
    private static final String defaultFieldName = "regularTypeArgument";
    private Context ctx;

    @BeforeEach
    public void setUp(@Source ScanResult source) {
        this.ctx = new Context(source);
    }

    @DisplayName("It should get annotations")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetAnnotations(TypeArgumentModel model, ModelKind kind,
            String name) {
        assertEquals(List.of(Sample.Foo.class.getName()),
                model.getAnnotations().stream()
                        .map(AnnotationInfoModel::getName)
                        .collect(Collectors.toList()));
    }

    @DisplayName("It should have correct widlcard")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_HaveCorrectWildcard(TypeArgumentModel model,
            ModelKind kind, String name) {
        switch (name) {
        case "anyTypeArgument":
            assertEquals(TypeArgumentModel.Wildcard.ANY, model.getWildcard());
            break;
        case "extendsTypeArgument":
            assertEquals(TypeArgumentModel.Wildcard.EXTENDS,
                    model.getWildcard());
            break;
        case "regularTypeArgument":
            assertEquals(TypeArgumentModel.Wildcard.NONE, model.getWildcard());
            break;
        case "superTypeArgument":
            assertEquals(TypeArgumentModel.Wildcard.SUPER, model.getWildcard());
            break;
        }
    }

    @DisplayName("It should have the same hashCode for source and reflection models")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveSameHashCodeForSourceAndReflectionModels(
            TypeArgumentModel reflectionModel, TypeArgumentModel sourceModel,
            String name) {
        assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
    }

    @DisplayName("It should have source and reflection models equal")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveSourceAndReflectionModelsEqual(
            TypeArgumentModel reflectionModel, TypeArgumentModel sourceModel,
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
    public void should_ProvideCorrectOrigin(TypeArgumentModel model,
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

    static final class Context
            extends AbstractContext<AnnotatedType, TypeArgument> {
        private static final Map<String, AnnotatedType> reflectionOrigins = getDeclaredFields(
                Sample.class)
                .collect(Collectors.toMap(Field::getName,
                        field -> ((AnnotatedParameterizedType) field
                                .getAnnotatedType())
                                .getAnnotatedActualTypeArguments()[0]));

        Context(ScanResult source) {
            super(source, reflectionOrigins,
                    getDeclaredFields(Sample.class, source)
                            .collect(Collectors.toMap(FieldInfo::getName,
                                    field -> ((ClassRefTypeSignature) field
                                            .getTypeSignatureOrTypeDescriptor())
                                            .getTypeArguments().get(0))));
        }

        Context(ExtensionContext context) {
            this(SourceExtension.getSource(context));
        }
    }

    static final class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{1} [{2}]";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context(context);

            return Streams.combine(
                    ctx.getReflectionOrigins().entrySet().stream()
                            .map(entry -> Arguments.of(
                                    TypeArgumentModel.of(entry.getValue()),
                                    ModelKind.REFLECTION, entry.getKey())),
                    ctx.getSourceOrigins().entrySet().stream()
                            .map(entry -> Arguments.of(
                                    TypeArgumentModel.of(entry.getValue()),
                                    ModelKind.SOURCE, entry.getKey())));
        }

        static final class Equality implements ArgumentsProvider {
            private static final String testNamePattern = "{2}";

            @Override
            public Stream<Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context(context);

                return ctx.getReflectionOrigins().entrySet().stream()
                        .map(entry -> Arguments.of(
                                TypeArgumentModel.of(entry.getValue()),
                                TypeArgumentModel.of(
                                        ctx.getSourceOrigin(entry.getKey())),
                                entry.getKey()));
            }
        }

        static class Checker extends SpecializationChecker<SpecializedModel> {
            Checker() {
                super(SpecializedModel.class,
                        getDeclaredMethods(SpecializedModel.class));
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
        public void should_HaveSpecialization(TypeArgumentModel model,
                ModelKind kind, String name) {
            checker.apply(model, "isTypeArgument", "isNonJDKClass");
        }
    }

    static class Sample {
        List<@Foo ?> anyTypeArgument;
        List<@Foo ? extends Association> extendsTypeArgument;
        List<@Foo Association> regularTypeArgument;
        List<@Foo ? super Association> superTypeArgument;

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE_USE)
        @interface Foo {
        }

        static class Association {
        }
    }
}
