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
import java.util.stream.Stream;

import org.apache.commons.lang3.function.Failable;
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

import dev.hilla.parser.test.helpers.BaseTestContext;
import dev.hilla.parser.test.helpers.ModelKind;
import dev.hilla.parser.test.helpers.Source;
import dev.hilla.parser.test.helpers.SourceExtension;
import dev.hilla.parser.test.helpers.SpecializationChecker;
import dev.hilla.parser.utils.Streams;

import io.github.classgraph.FieldInfo;
import io.github.classgraph.ScanResult;

@ExtendWith(SourceExtension.class)
public class FieldInfoModelTests {
    private final CharacteristicsModelProvider.Checker characteristicsChecker = new CharacteristicsModelProvider.Checker();

    @DisplayName("It should detect field characteristics correctly")
    @ParameterizedTest(name = CharacteristicsModelProvider.testName)
    @ArgumentsSource(CharacteristicsModelProvider.class)
    public void should_DetectCharacteristics(FieldInfoModel model,
            Object origin, String[] characteristics, ModelKind kind,
            CharacteristicsModelProvider.Context context, String testName) {
        characteristicsChecker.apply(model, characteristics);
    }

    @DisplayName("It should provide field dependencies")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetDependencies(FieldInfoModel model, Object origin,
            ModelKind kind, ModelProvider.Context context) {
        var expected = Set.of(ClassInfoModel.of(Sample.Dependency.class));
        var actual = model.getDependencies();

        assertEquals(expected, actual);
    }

    @DisplayName("It should provide field name")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetName(FieldInfoModel model, Object origin,
            ModelKind kind, ModelProvider.Context context) {
        assertEquals("field", model.getName());
    }

    @DisplayName("It should get the field's type")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetType(FieldInfoModel model, Object origin,
            ModelKind kind, ModelProvider.Context context) {
        SignatureModel expected = null;

        switch (kind) {
        case REFLECTION:
            expected = SignatureModel.of(((Field) origin).getAnnotatedType());
            break;
        case SOURCE:
            expected = SignatureModel.of(
                    ((FieldInfo) origin).getTypeSignatureOrTypeDescriptor());
            break;
        }

        assertEquals(expected, model.getType());
    }

    @DisplayName("It should have the same hashCode for source and reflection models")
    @Test
    public void should_HaveSameHashCodeForSourceAndReflectionModels(
            @Source ScanResult scanResult) throws NoSuchFieldException {
        var reflectionModel = getDefaultReflectionModel();
        var sourceModel = getDefaultSourceModel(scanResult);

        assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
    }

    @DisplayName("It should have source and reflection models equal")
    @Test
    public void should_HaveSourceAndReflectionModelsEqual(
            @Source ScanResult scanResult) throws NoSuchFieldException {
        var reflectionModel = getDefaultReflectionModel();
        var sourceModel = getDefaultSourceModel(scanResult);

        assertEquals(reflectionModel, reflectionModel);
        assertEquals(reflectionModel, sourceModel);

        assertEquals(sourceModel, sourceModel);
        assertEquals(sourceModel, reflectionModel);

        assertNotEquals(sourceModel, new Object());
        assertNotEquals(reflectionModel, new Object());
    }

    private FieldInfoModel getDefaultReflectionModel()
            throws NoSuchFieldException {
        return FieldInfoModel.of(Sample.class.getDeclaredField("field"));
    }

    private FieldInfoModel getDefaultSourceModel(ScanResult scanResult) {
        return FieldInfoModel.of(scanResult.getClassInfo(Sample.class.getName())
                .getDeclaredFieldInfo("field"));
    }

    public static final class CharacteristicsModelProvider
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
                extends SpecializationChecker<FieldInfoModel> {
            private static final List<String> allowedMethods = List.of("isEnum",
                    "isFinal", "isPrivate", "isProtected", "isPublic",
                    "isStatic", "isSynthetic", "isTransient");

            public Checker() {
                super(FieldInfoModel.class,
                        Arrays.stream(FieldInfoModel.class.getDeclaredMethods())
                                .filter(method -> allowedMethods
                                        .contains(method.getName())));
            }
        }

        public static final class Context extends BaseTestContext {
            private static final Map<Map.Entry<Class<?>, String>, String[]> associations = Map
                    .ofEntries(
                            entry(Map.entry(Characteristics.class,
                                    "publicField"), "isPublic"),
                            entry(Map.entry(Characteristics.class,
                                    "protectedField"), "isProtected"),
                            entry(Map.entry(Characteristics.class,
                                    "privateField"), "isPrivate"),
                            entry(Map.entry(Characteristics.class,
                                    "staticField"), "isPrivate", "isStatic"),
                            entry(Map.entry(Characteristics.class,
                                    "finalField"), "isFinal", "isPrivate"),
                            entry(Map.entry(Characteristics.class,
                                    "publicStaticFinalField"), "isFinal",
                                    "isPublic", "isStatic"),
                            entry(Map.entry(Characteristics.class,
                                    "transientField"), "isPublic",
                                    "isTransient"),
                            entry(Map.entry(Characteristics.Enum.class,
                                    "ENUM_FIELD"), "isEnum", "isFinal",
                                    "isPublic", "isStatic"));

            Context(ExtensionContext context) {
                super(context);
            }

            public Stream<Arguments> getReflectionArguments() {
                return associations.entrySet().stream()
                        .map(Failable.asFunction(entry -> {
                            var origin = getReflectionOrigin(entry.getKey());
                            var model = FieldInfoModel.of(origin);

                            return Arguments.of(model, origin, entry.getValue(),
                                    ModelKind.REFLECTION, this,
                                    origin.getName());
                        }));
            }

            public Stream<Arguments> getSourceArguments() {
                return associations.entrySet().stream().map(entry -> {
                    var origin = getSourceOrigin(entry.getKey());
                    var model = FieldInfoModel.of(origin);

                    return Arguments.of(model, origin, entry.getValue(),
                            ModelKind.REFLECTION, this, origin.getName());
                });
            }

            private Field getReflectionOrigin(
                    Map.Entry<Class<?>, String> searchInfo)
                    throws NoSuchFieldException {
                return searchInfo.getKey()
                        .getDeclaredField(searchInfo.getValue());
            }

            private FieldInfo getSourceOrigin(
                    Map.Entry<Class<?>, String> searchInfo) {
                return getScanResult()
                        .getClassInfo(searchInfo.getKey().getName())
                        .getDeclaredFieldInfo(searchInfo.getValue());
            }
        }
    }

    public static final class ModelProvider implements ArgumentsProvider {
        public static final String testName = "{2}";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) throws Exception {
            var ctx = new Context(context);

            return Stream.of(ctx.getReflectionArguments(),
                    ctx.getSourceArguments());
        }

        public static final class Context extends BaseTestContext {
            Context(ExtensionContext context) {
                super(context);
            }

            public Arguments getReflectionArguments()
                    throws NoSuchFieldException {
                var origin = Sample.class.getDeclaredField("field");
                var model = FieldInfoModel.of(origin);

                return Arguments.of(model, origin, ModelKind.REFLECTION, this);
            }

            public Arguments getSourceArguments() {
                var origin = getScanResult()
                        .getClassInfo(Sample.class.getName())
                        .getDeclaredFieldInfo("field");
                var model = FieldInfoModel.of(origin);

                return Arguments.of(model, origin, ModelKind.SOURCE, this);
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
        private AnnotationInfoModel annotation;

        @BeforeEach
        public void setUp() throws NoSuchFieldException {
            var annotationOrigin = (Annotation) Sample.class
                    .getDeclaredField("field").getAnnotation(Sample.Foo.class);

            annotation = AnnotationInfoModel.of(annotationOrigin);
        }

        @DisplayName("It should access annotation")
        @ParameterizedTest(name = ModelProvider.testName)
        @ArgumentsSource(ModelProvider.class)
        public void should_AccessAnnotation(FieldInfoModel model, Object origin,
                ModelKind kind, ModelProvider.Context context) {
            assertEquals(List.of(annotation), model.getAnnotations());
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
}
