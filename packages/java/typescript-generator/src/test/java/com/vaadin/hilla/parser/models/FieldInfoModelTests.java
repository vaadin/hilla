package com.vaadin.hilla.parser.models;

import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredField;
import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredFields;
import static com.vaadin.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethods;
import static com.vaadin.hilla.parser.test.helpers.SpecializationChecker.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
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

import com.vaadin.hilla.parser.test.helpers.ModelKind;
import com.vaadin.hilla.parser.test.helpers.Source;
import com.vaadin.hilla.parser.test.helpers.SourceExtension;
import com.vaadin.hilla.parser.test.helpers.SpecializationChecker;
import com.vaadin.hilla.parser.test.helpers.context.AbstractCharacteristics;
import com.vaadin.hilla.parser.test.helpers.context.AbstractContext;
import com.vaadin.hilla.parser.utils.Streams;

import io.github.classgraph.FieldInfo;
import io.github.classgraph.ScanResult;

@ExtendWith(SourceExtension.class)
public class FieldInfoModelTests {
    private Context.Default ctx;

    @BeforeEach
    public void setUp(@Source ScanResult source) {
        ctx = new Context.Default(source);
    }

    @DisplayName("It should provide field name")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetName(FieldInfoModel model, ModelKind kind) {
        assertEquals("field", model.getName());
    }

    @DisplayName("It should get the field's type")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetType(FieldInfoModel model, ModelKind kind) {
        SignatureModel expected = null;

        switch (kind) {
        case REFLECTION:
            expected = SignatureModel
                    .of(ctx.getReflectionOrigin().getAnnotatedType());
            break;
        case SOURCE:
            expected = SignatureModel.of(
                    ctx.getSourceOrigin().getTypeSignatureOrTypeDescriptor());
            break;
        }

        assertEquals(expected, model.getType());
    }

    @DisplayName("It should have the same hashCode for source and reflection models")
    @Test
    public void should_HaveSameHashCodeForSourceAndReflectionModels() {
        var reflectionModel = FieldInfoModel.of(ctx.getReflectionOrigin());
        var sourceModel = FieldInfoModel.of(ctx.getSourceOrigin());

        assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
    }

    @DisplayName("It should have source and reflection models equal")
    @Test
    public void should_HaveSourceAndReflectionModelsEqual() {
        var reflectionModel = FieldInfoModel.of(ctx.getReflectionOrigin());
        var sourceModel = FieldInfoModel.of(ctx.getSourceOrigin());

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
    public void should_ProvideCorrectOrigin(FieldInfoModel model,
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

    public static final class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{1}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context.Default(context);

            return Stream.of(
                    Arguments.of(FieldInfoModel.of(ctx.getReflectionOrigin()),
                            ModelKind.REFLECTION),
                    Arguments.of(FieldInfoModel.of(ctx.getSourceOrigin()),
                            ModelKind.SOURCE));
        }

        public static final class Characteristics implements ArgumentsProvider {
            public static final String testNamePattern = "{2} [{3}]";

            @Override
            public Stream<Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context.Characteristics(context);

                return Streams.combine(
                        ctx.getReflectionCharacteristics().entrySet().stream()
                                .map(entry -> Arguments.of(
                                        FieldInfoModel.of(entry.getKey()),
                                        entry.getValue(), ModelKind.REFLECTION,
                                        entry.getKey().getName())),
                        ctx.getSourceCharacteristics().entrySet().stream()
                                .map(entry -> Arguments.of(
                                        FieldInfoModel.of(entry.getKey()),
                                        entry.getValue(), ModelKind.SOURCE,
                                        entry.getKey().getName())));
            }

        }

        public static final class CharacterizedChecker
                extends SpecializationChecker<FieldInfoModel> {
            private static final List<String> allowedMethods = List.of("isEnum",
                    "isFinal", "isPrivate", "isProtected", "isPublic",
                    "isStatic", "isSynthetic", "isTransient");

            public CharacterizedChecker() {
                super(FieldInfoModel.class,
                        getDeclaredMethods(FieldInfoModel.class),
                        allowedMethods);
            }
        }
    }

    static final class Sample {
        @Foo
        private Dependency field;

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.FIELD)
        @interface Foo {
        }

        static final class Dependency {
        }
    }

    @Nested
    @DisplayName("As an AnnotatedModel")
    public class AsAnnotatedModel {
        @DisplayName("It should access annotation")
        @ParameterizedTest(name = ModelProvider.testNamePattern)
        @ArgumentsSource(ModelProvider.class)
        public void should_AccessAnnotation(FieldInfoModel model,
                ModelKind kind) {
            assertEquals(List.of(AnnotationInfoModel.of(ctx.getAnnotation())),
                    model.getAnnotations());
        }
    }

    @DisplayName("As a field model with characteristics")
    @Nested
    public class AsCharacterizedFieldModel {
        private final ModelProvider.CharacterizedChecker checker = new ModelProvider.CharacterizedChecker();

        @DisplayName("It should detect field characteristics correctly")
        @ParameterizedTest(name = ModelProvider.Characteristics.testNamePattern)
        @ArgumentsSource(ModelProvider.Characteristics.class)
        public void should_DetectCharacteristics(FieldInfoModel model,
                String[] characteristics, ModelKind kind, String testName) {
            checker.apply(model, characteristics);
        }
    }

    static class Characteristics {
        public static final String publicStaticFinalField = "";
        private static String staticField;
        private final String finalField = "";
        public String publicField;
        public transient String transientField;
        protected String protectedField;
        private String privateField;

        public enum Enum {
            ENUM_FIELD,
        }
    }

    static abstract class Context {
        private final ScanResult source;

        Context(ScanResult source) {
            this.source = source;
        }

        public ScanResult getSource() {
            return source;
        }

        static final class Characteristics
                extends AbstractCharacteristics<Field, FieldInfo> {
            private static final Map<Field, String[]> reflectionAssociations;

            static {
                var refClass = FieldInfoModelTests.Characteristics.class;
                var refEnumClass = FieldInfoModelTests.Characteristics.Enum.class;
                reflectionAssociations = Map.ofEntries(
                        entry(getDeclaredField(refClass, "publicField"),
                                "isPublic"),
                        entry(getDeclaredField(refClass, "protectedField"),
                                "isProtected"),
                        entry(getDeclaredField(refClass, "privateField"),
                                "isPrivate"),
                        entry(getDeclaredField(refClass, "staticField"),
                                "isPrivate", "isStatic"),
                        entry(getDeclaredField(refClass, "finalField"),
                                "isFinal", "isPrivate"),
                        entry(getDeclaredField(refClass,
                                "publicStaticFinalField"), "isFinal",
                                "isPublic", "isStatic"),
                        entry(getDeclaredField(refClass, "transientField"),
                                "isPublic", "isTransient"),
                        entry(getDeclaredField(refEnumClass, "ENUM_FIELD"),
                                "isEnum", "isFinal", "isPublic", "isStatic"));
            }

            Characteristics(ExtensionContext context) {
                this(SourceExtension.getSource(context));
            }

            Characteristics(ScanResult source) {
                super(source, reflectionAssociations,
                        reflectionAssociations.entrySet().stream()
                                .collect(Collectors.toMap(
                                        entry -> getDeclaredField(
                                                entry.getKey(), source),
                                        Map.Entry::getValue)));
            }
        }

        static final class Default extends AbstractContext<Field, FieldInfo> {
            private static final Annotation annotation;
            private static final String defaultFieldName = "field";
            private static final Map<String, Field> reflectionOrigins = getDeclaredFields(
                    Sample.class)
                    .collect(Collectors.toMap(Field::getName,
                            Function.identity()));

            static {
                annotation = reflectionOrigins.get(defaultFieldName)
                        .getAnnotation(Sample.Foo.class);
            }

            Default(ExtensionContext context) {
                this(SourceExtension.getSource(context));
            }

            Default(ScanResult source) {
                super(source, reflectionOrigins,
                        getDeclaredFields(Sample.class, source)
                                .collect(Collectors.toMap(FieldInfo::getName,
                                        Function.identity())));
            }

            public Annotation getAnnotation() {
                return annotation;
            }

            public Field getReflectionOrigin() {
                return getReflectionOrigin(defaultFieldName);
            }

            public FieldInfo getSourceOrigin() {
                return getSourceOrigin(defaultFieldName);
            }
        }
    }
}
