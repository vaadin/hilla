package dev.hilla.parser.models;

import static dev.hilla.parser.test.helpers.SpecializationChecker.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
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

import io.github.classgraph.FieldInfo;
import io.github.classgraph.ScanResult;

@ExtendWith(SourceExtension.class)
public class FieldInfoModelTests {
    private Context.Default ctx;

    @BeforeEach
    public void setUp(@Source ScanResult source) throws NoSuchFieldException {
        ctx = new Context.Default(source);
    }

    @DisplayName("It should provide field dependencies")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetDependencies(FieldInfoModel model, ModelKind kind) {
        var expected = Set.of(ClassInfoModel.of(Sample.Dependency.class));
        var actual = model.getDependencies();

        assertEquals(expected, actual);
    }

    @DisplayName("It should provide field name")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetName(FieldInfoModel model, ModelKind kind) {
        assertEquals("field", model.getName());
    }

    @DisplayName("It should get the field's type")
    @ParameterizedTest(name = ModelProvider.testName)
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

    public static final class ModelProvider implements ArgumentsProvider {
        public static final String testName = "{1}";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) throws NoSuchFieldException {
            var ctx = new Context.Default(context);

            return Stream.of(
                    Arguments.of(FieldInfoModel.of(ctx.getReflectionOrigin()),
                            ModelKind.REFLECTION),
                    Arguments.of(FieldInfoModel.of(ctx.getSourceOrigin()),
                            ModelKind.SOURCE));
        }

        public static final class Characteristics implements ArgumentsProvider {
            public static final String testName = "{2} [{3}]";

            @Override
            public Stream<? extends Arguments> provideArguments(
                    ExtensionContext context) throws NoSuchFieldException {
                var ctx = new Context.Characteristics(context);

                return Streams.combine(
                        ctx.getReflectionAssociations().entrySet().stream()
                                .map(entry -> Arguments.of(
                                        FieldInfoModel.of(entry.getKey()),
                                        entry.getValue(), ModelKind.REFLECTION,
                                        entry.getKey().getName())),
                        ctx.getSourceAssociations().entrySet().stream()
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
                        Arrays.stream(FieldInfoModel.class.getDeclaredMethods())
                                .filter(method -> allowedMethods
                                        .contains(method.getName())));
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
        @ParameterizedTest(name = ModelProvider.testName)
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
        @ParameterizedTest(name = ModelProvider.Characteristics.testName)
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

        static final class Characteristics extends Context {
            private final Map<Field, String[]> reflectionAssociations;
            private final Map<FieldInfo, String[]> sourceAssociations;

            Characteristics(ExtensionContext context)
                    throws NoSuchFieldException {
                this(SourceExtension.getSource(context));
            }

            Characteristics(ScanResult source) throws NoSuchFieldException {
                super(source);

                var refClass = FieldInfoModelTests.Characteristics.class;
                var refEnumClass = FieldInfoModelTests.Characteristics.Enum.class;
                reflectionAssociations = Map.ofEntries(
                        entry(refClass.getDeclaredField("publicField"),
                                "isPublic"),
                        entry(refClass.getDeclaredField("protectedField"),
                                "isProtected"),
                        entry(refClass.getDeclaredField("privateField"),
                                "isPrivate"),
                        entry(refClass.getDeclaredField("staticField"),
                                "isPrivate", "isStatic"),
                        entry(refClass.getDeclaredField("finalField"),
                                "isFinal", "isPrivate"),
                        entry(refClass
                                .getDeclaredField("publicStaticFinalField"),
                                "isFinal", "isPublic", "isStatic"),
                        entry(refClass.getDeclaredField("transientField"),
                                "isPublic", "isTransient"),
                        entry(refEnumClass.getDeclaredField("ENUM_FIELD"),
                                "isEnum", "isFinal", "isPublic", "isStatic"));

                sourceAssociations = reflectionAssociations.entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> source
                                        .getClassInfo(entry.getKey()
                                                .getDeclaringClass().getName())
                                        .getFieldInfo(entry.getKey().getName()),
                                Map.Entry::getValue));

            }

            public Map<Field, String[]> getReflectionAssociations() {
                return reflectionAssociations;
            }

            public Map<FieldInfo, String[]> getSourceAssociations() {
                return sourceAssociations;
            }
        }

        static final class Default extends Context {
            private final Annotation annotation;
            private final Field reflectionOrigin;
            private final FieldInfo sourceOrigin;

            Default(ExtensionContext context) throws NoSuchFieldException {
                this(SourceExtension.getSource(context));
            }

            Default(ScanResult source) throws NoSuchFieldException {
                super(source);

                this.reflectionOrigin = Sample.class.getDeclaredField("field");
                this.sourceOrigin = source.getClassInfo(Sample.class.getName())
                        .getDeclaredFieldInfo("field");
                this.annotation = Sample.class.getDeclaredField("field")
                        .getAnnotation(Sample.Foo.class);
            }

            public Annotation getAnnotation() {
                return annotation;
            }

            public Field getReflectionOrigin() {
                return reflectionOrigin;
            }

            public FieldInfo getSourceOrigin() {
                return sourceOrigin;
            }
        }
    }
}
