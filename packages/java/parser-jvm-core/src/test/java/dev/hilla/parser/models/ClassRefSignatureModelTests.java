package dev.hilla.parser.models;

import static dev.hilla.parser.test.helpers.ClassMemberUtils.getClassInfo;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredField;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredFields;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethods;
import static dev.hilla.parser.test.helpers.SpecializationChecker.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

import dev.hilla.parser.test.helpers.Source;
import dev.hilla.parser.test.helpers.SourceExtension;
import dev.hilla.parser.test.helpers.SpecializationChecker;
import dev.hilla.parser.utils.Streams;

import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.ScanResult;

@ExtendWith(SourceExtension.class)
public class ClassRefSignatureModelTests {
    private Context.Default ctx;
    private Context.Matches matches;

    @BeforeEach
    public void setUp(@Source ScanResult source) {
        ctx = new Context.Default(source);
        matches = new Context.Matches();
    }

    @DisplayName("It should compare parametrized class reference signature with its class info")
    @Test
    public void should_CompareParametrizedClassRefWithClassInfo() {
        var fieldName = "staticParametrizedDependency";

        var bareParametrized = ctx.getBareReflectionOrigin(fieldName);

        var completeParametrizedHidden = ctx
                .getCompleteReflectionOrigin(fieldName);
        var completeParametrized = (AnnotatedParameterizedType) completeParametrizedHidden;

        var sourceParametrized = ctx.getSourceOrigin(fieldName);

        var classParametrized = Sample.StaticParametrizedDependency.Sub.class;
        var classInfoParametrized = getClassInfo(classParametrized,
                ctx.getSource());

        assertTrue(
                ClassRefSignatureModel.is(bareParametrized, classParametrized));
        assertTrue(ClassRefSignatureModel.is(completeParametrized,
                classParametrized));
        assertTrue(ClassRefSignatureModel.is(completeParametrizedHidden,
                classParametrized));
        assertTrue(ClassRefSignatureModel.is(sourceParametrized,
                classParametrized));

        assertTrue(ClassRefSignatureModel.is(bareParametrized,
                classInfoParametrized));
        assertTrue(ClassRefSignatureModel.is(completeParametrized,
                classInfoParametrized));
        assertTrue(ClassRefSignatureModel.is(completeParametrizedHidden,
                classInfoParametrized));
        assertTrue(ClassRefSignatureModel.is(sourceParametrized,
                classInfoParametrized));
    }

    @DisplayName("It should compare simple class reference signature with its class info")
    @Test
    public void should_CompareSimpleClassRefWithClassInfo() {
        var fieldName = "staticDependency";

        var bareSimple = ctx.getBareReflectionOrigin(fieldName);
        var completeSimple = ctx.getCompleteReflectionOrigin(fieldName);
        var sourceSimple = ctx.getSourceOrigin(fieldName);

        var classSimple = Sample.StaticDependency.Sub.class;
        var classInfoSimple = ctx.getSource()
                .getClassInfo(classSimple.getName());

        assertTrue(ClassRefSignatureModel.is(bareSimple, classSimple));
        assertTrue(ClassRefSignatureModel.is(completeSimple, classSimple));
        assertTrue(ClassRefSignatureModel.is(sourceSimple, classSimple));

        assertTrue(ClassRefSignatureModel.is(bareSimple, classInfoSimple));
        assertTrue(ClassRefSignatureModel.is(completeSimple, classInfoSimple));
        assertTrue(ClassRefSignatureModel.is(sourceSimple, classInfoSimple));
    }

    @DisplayName("It should get class info")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetClassInfo(ClassRefSignatureModel model,
            ModelKind kind) {
        var cls = ClassInfoModel
                .of(Sample.DynamicParametrizedDependency.Sub.class);

        assertEquals(cls, model.getClassInfo());

        switch (kind) {
        case REFLECTION_COMPLETE:
        case SOURCE: {
            var owner = ClassInfoModel
                    .of(Sample.DynamicParametrizedDependency.class);
            var grandOwner = ClassInfoModel.of(Sample.class);
            var grandGrandOwner = ClassInfoModel.of(getClass());

            assertEquals(owner, model.getOwner()
                    .map(ClassRefSignatureModel::getClassInfo).orElse(null));

            assertEquals(grandOwner,
                    model.getOwner().flatMap(ClassRefSignatureModel::getOwner)
                            .map(ClassRefSignatureModel::getClassInfo)
                            .orElse(null));

            assertEquals(grandGrandOwner,
                    model.getOwner().flatMap(ClassRefSignatureModel::getOwner)
                            .flatMap(ClassRefSignatureModel::getOwner)
                            .map(ClassRefSignatureModel::getClassInfo)
                            .orElse(null));
        }
            break;
        case REFLECTION_BARE:
            break;
        }
    }

    @DisplayName("It should get type arguments as a stream")
    @Test
    public void should_GetTypeArgumentsAsStream(@Source ScanResult source)
            throws NoSuchFieldException {
        var ctx = new Context.Default(source);
        var model = ClassRefSignatureModel
                .of(ctx.getCompleteReflectionOrigin(ModelProvider.fieldName));
        var expected = List.of(TypeArgumentModel
                .of(matches.getTypeArgument("intTypeArgument")));
        var actual = model.getTypeArgumentsStream()
                .collect(Collectors.toList());

        assertEquals(expected, actual);
    }

    @DisplayName("It should get type owner")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetTypeOwner(ClassRefSignatureModel model,
            ModelKind kind) {
        switch (kind) {
        case REFLECTION_COMPLETE:
        case SOURCE: {
            assertEquals(Sample.DynamicParametrizedDependency.class.getName(),
                    model.getOwner().map(ClassRefSignatureModel::getName)
                            .orElse(null));

            assertEquals(Sample.class.getName(),
                    model.getOwner().flatMap(ClassRefSignatureModel::getOwner)
                            .map(ClassRefSignatureModel::getName).orElse(null));

            assertEquals(getClass().getName(),
                    model.getOwner().flatMap(ClassRefSignatureModel::getOwner)
                            .flatMap(ClassRefSignatureModel::getOwner)
                            .map(ClassRefSignatureModel::getName).orElse(null));

            assertEquals(Optional.empty(),
                    model.getOwner().flatMap(ClassRefSignatureModel::getOwner)
                            .flatMap(ClassRefSignatureModel::getOwner)
                            .flatMap(ClassRefSignatureModel::getOwner));
        }
            break;
        case REFLECTION_BARE:
            assertEquals(Optional.empty(), model.getOwner());
            break;
        }
    }

    @DisplayName("It should have the same hashCode for source and reflection models")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveSameHashCodeForSourceAndReflectionModels(
            AnnotatedType reflectionOrigin, ClassRefTypeSignature sourceOrigin,
            String testName) {
        var reflectionModel = ClassRefSignatureModel.of(reflectionOrigin);
        var sourceModel = ClassRefSignatureModel.of(sourceOrigin);

        assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
    }

    @DisplayName("It should have source and reflection models equal")
    @ParameterizedTest(name = ModelProvider.Equality.testNamePattern)
    @ArgumentsSource(ModelProvider.Equality.class)
    public void should_HaveSourceAndReflectionModelsEqual(
            AnnotatedType reflectionOrigin, ClassRefTypeSignature sourceOrigin,
            String testName) {
        var reflectionModel = ClassRefSignatureModel.of(reflectionOrigin);
        var sourceModel = ClassRefSignatureModel.of(sourceOrigin);

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
    public void should_ProvideCorrectOrigin(ClassRefSignatureModel model,
            ModelKind kind) {
        switch (kind) {
        case REFLECTION_COMPLETE:
            assertEquals(
                    ctx.getCompleteReflectionOrigin(ModelProvider.fieldName),
                    model.get());
            assertTrue(model.isReflection());
            break;
        case REFLECTION_BARE:
            assertEquals(ctx.getBareReflectionOrigin(ModelProvider.fieldName),
                    model.get());
            assertTrue(model.isReflection());
            break;
        case SOURCE:
            assertEquals(ctx.getSourceOrigin(ModelProvider.fieldName),
                    model.get());
            assertTrue(model.isSource());
            break;
        }
    }

    @DisplayName("It should resolve underlying class correctly")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ResolveUnderlyingClass(ClassRefSignatureModel model,
            ModelKind kind) {
        assertEquals(Sample.DynamicParametrizedDependency.Sub.class.getName(),
                model.getName());
    }

    @DisplayName("It should resolve underlying type arguments")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ResolveUnderlyingTypeArguments(
            ClassRefSignatureModel model, ModelKind kind)
            throws NoSuchFieldException {
        switch (kind) {
        case REFLECTION_COMPLETE:
        case SOURCE: {
            var intTypeArgument = TypeArgumentModel
                    .of(matches.getTypeArgument("intTypeArgument"));
            var stringTypeArgument = TypeArgumentModel
                    .of(matches.getTypeArgument("stringTypeArgument"));

            assertEquals(List.of(intTypeArgument), model.getTypeArguments());

            assertEquals(List.of(stringTypeArgument),
                    model.getOwner()
                            .map(ClassRefSignatureModel::getTypeArguments)
                            .orElseGet(List::of));
        }
            break;
        case REFLECTION_BARE:
            assertEquals(List.of(), model.getTypeArguments());
            break;
        }
    }

    enum ModelKind {
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

    static final class ModelProvider implements ArgumentsProvider {
        public static final String fieldName = "dynamicParametrizedDependency";
        public static final String testNamePattern = "{1}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context.Default(context);

            var complete = ctx.getCompleteReflectionOrigin(fieldName);
            var bare = ctx.getBareReflectionOrigin(fieldName);
            var source = ctx.getSourceOrigin(fieldName);

            return Stream.of(
                    Arguments.of(ClassRefSignatureModel.of(complete),
                            ModelKind.REFLECTION_COMPLETE),
                    Arguments.of(ClassRefSignatureModel.of(bare),
                            ModelKind.REFLECTION_BARE),
                    Arguments.of(ClassRefSignatureModel.of(source),
                            ModelKind.SOURCE));
        }

        static final class Checker
                extends SpecializationChecker<SpecializedModel> {
            public Checker() {
                super(SpecializedModel.class,
                        getDeclaredMethods(SpecializedModel.class));
            }
        }

        static final class Equality implements ArgumentsProvider {
            public static final String testNamePattern = "BOTH [{2}]";

            @Override
            public Stream<Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context.Default(context);

                return ctx.getNames().stream().map(name -> {
                    var complete = ctx.getCompleteReflectionOrigin(name);
                    var source = ctx.getSourceOrigin(name);

                    return Arguments.of(complete, source, name);
                });
            }
        }

        static final class Specialized implements ArgumentsProvider {
            public static final String testNamePattern = "{2} [{3}]";

            private static final Map<String, String[]> specializations = Map
                    .ofEntries(
                            entry(BigDecimal.class.getName(), "isBigDecimal",
                                    "isClassRef", "isJDKClass"),
                            entry(BigInteger.class.getName(), "isBigInteger",
                                    "isClassRef", "isJDKClass"),
                            entry(Boolean.class.getName(), "isBoolean",
                                    "isClassRef", "isJDKClass"),
                            entry(Byte.class.getName(), "hasIntegerType",
                                    "isByte", "isClassRef", "isJDKClass"),
                            entry(Character.class.getName(), "isCharacter",
                                    "isClassRef", "isJDKClass"),
                            entry(Double.class.getName(), "hasFloatType",
                                    "isDouble", "isClassRef", "isJDKClass"),
                            entry(Float.class.getName(), "hasFloatType",
                                    "isFloat", "isClassRef", "isJDKClass"),
                            entry(List.class.getName(), "isIterable",
                                    "isClassRef", "isJDKClass"),
                            entry(Long.class.getName(), "hasIntegerType",
                                    "isLong", "isClassRef", "isJDKClass"),
                            entry(Short.class.getName(), "hasIntegerType",
                                    "isShort", "isClassRef", "isJDKClass"),
                            entry(Sample.Characteristics.Enum.class.getName(),
                                    "isEnum", "isClassRef", "isNonJDKClass"),
                            entry(Integer.class.getName(), "hasIntegerType",
                                    "isInteger", "isClassRef", "isJDKClass"),
                            entry(Date.class.getName(), "isDate", "isClassRef",
                                    "isJDKClass"),
                            entry(LocalDateTime.class.getName(), "isDateTime",
                                    "isClassRef", "isJDKClass"),
                            entry(Map.class.getName(), "isMap", "isClassRef",
                                    "isJDKClass"),
                            entry(Object.class.getName(), "isNativeObject",
                                    "isClassRef", "isJDKClass"),
                            entry(Optional.class.getName(), "isOptional",
                                    "isClassRef", "isJDKClass"),
                            entry(String.class.getName(), "isString",
                                    "isClassRef", "isJDKClass"));

            @Override
            public Stream<Arguments> provideArguments(
                    ExtensionContext context) {
                var ctx = new Context.Characteristics(context);

                var complete = ctx.getCompleteReflectionOrigins().entrySet()
                        .stream().map(entry -> {
                            var origin = entry.getValue();
                            var name = origin instanceof AnnotatedParameterizedType
                                    ? ((Class<?>) ((ParameterizedType) origin
                                            .getType()).getRawType()).getName()
                                    : ((Class<?>) origin.getType()).getName();

                            return Arguments.of(SignatureModel.of(origin),
                                    specializations.get(name),
                                    ModelKind.REFLECTION_COMPLETE,
                                    entry.getKey());
                        });
                var bare = ctx.getBareReflectionOrigins().entrySet().stream()
                        .map(entry -> Arguments.of(
                                SignatureModel.of(entry.getValue()),
                                specializations.get(entry.getValue().getName()),
                                ModelKind.REFLECTION_BARE, entry.getKey()));

                var source = ctx.getSourceOrigins().entrySet().stream()
                        .map(entry -> Arguments.of(
                                SignatureModel.of(entry.getValue()),
                                specializations.get(entry.getValue()
                                        .getFullyQualifiedClassName()),
                                ModelKind.SOURCE, entry.getKey()));

                return Streams.combine(complete, bare, source);
            }

        }
    }

    @Nested
    @DisplayName("As an AnnotatedModel")
    public class AsAnnotatedModel {
        @DisplayName("It should get a type annotation")
        @ParameterizedTest(name = ModelProvider.testNamePattern)
        @ArgumentsSource(ModelProvider.class)
        public void should_GetTypeAnnotation(ClassRefSignatureModel model,
                ModelKind kind) throws NoSuchFieldException {
            switch (kind) {
            case REFLECTION_COMPLETE:
            case SOURCE: {
                var fooAnnotation = AnnotationInfoModel
                        .of(matches.getAnnotation("fooAnnotation"));
                var barAnnotation = AnnotationInfoModel
                        .of(matches.getAnnotation("barAnnotation"));

                assertEquals(List.of(fooAnnotation), model.getAnnotations());

                assertEquals(List.of(barAnnotation),
                        model.getOwner()
                                .map(ClassRefSignatureModel::getAnnotations)
                                .orElseGet(List::of));
            }
                break;
            case REFLECTION_BARE:
                assertEquals(Optional.empty(), model.getOwner());
                break;
            }
        }
    }

    @Nested
    @DisplayName("As a SpecializedModel")
    public class AsSpecializedModel {
        private final ModelProvider.Checker checker = new ModelProvider.Checker();

        @DisplayName("It should have a class reference specialization")
        @ParameterizedTest(name = ModelProvider.Specialized.testNamePattern)
        @ArgumentsSource(ModelProvider.Specialized.class)
        public void should_HaveSpecialization(ClassRefSignatureModel model,
                String[] characteristics, ModelKind kind, String testName) {
            checker.apply(model, characteristics);
        }
    }

    abstract static class Context {
        private final Map<String, Class<?>> bareReflectionOrigins;
        private final Map<String, AnnotatedType> completeReflectionOrigins;
        private final ScanResult source;
        private final Map<String, ClassRefTypeSignature> sourceOrigins;

        Context(ScanResult source, ReflectionData data) {
            this.source = source;
            this.sourceOrigins = getDeclaredFields(data.getTarget(), source)
                    .collect(Collectors.toMap(FieldInfo::getName,
                            field -> (ClassRefTypeSignature) field
                                    .getTypeSignatureOrTypeDescriptor()));
            this.bareReflectionOrigins = data.getBareReflectionOrigins();
            this.completeReflectionOrigins = data
                    .getCompleteReflectionOrigins();
        }

        public Class<?> getBareReflectionOrigin(String name) {
            return bareReflectionOrigins.get(name);
        }

        public Map<String, Class<?>> getBareReflectionOrigins() {
            return bareReflectionOrigins;
        }

        public AnnotatedType getCompleteReflectionOrigin(String name) {
            return completeReflectionOrigins.get(name);
        }

        public Map<String, AnnotatedType> getCompleteReflectionOrigins() {
            return completeReflectionOrigins;
        }

        public Set<String> getNames() {
            return bareReflectionOrigins.keySet();
        }

        public ScanResult getSource() {
            return source;
        }

        public ClassRefTypeSignature getSourceOrigin(String name) {
            return sourceOrigins.get(name);
        }

        public Map<String, ClassRefTypeSignature> getSourceOrigins() {
            return sourceOrigins;
        }

        static final class Matches {
            public Annotation getAnnotation(String name)
                    throws NoSuchFieldException {
                var annotations = getDeclaredField(Sample.Matches.class, name)
                        .getAnnotatedType().getAnnotations();

                if (annotations.length > 0) {
                    return annotations[0];
                }

                throw new NoSuchFieldException(
                        "No annotated field with name \"" + name + "\" found");
            }

            public AnnotatedType getTypeArgument(String name)
                    throws NoSuchFieldException {
                var owner = getDeclaredField(Sample.Matches.class, name)
                        .getAnnotatedType();

                if (owner instanceof AnnotatedParameterizedType) {
                    return ((AnnotatedParameterizedType) owner)
                            .getAnnotatedActualTypeArguments()[0];
                }

                throw new NoSuchFieldException(
                        "No parametrized field with name \"" + name
                                + "\" found");
            }
        }

        static final class ReflectionData {
            private final Map<String, Class<?>> bareReflectionOrigins;
            private final Map<String, AnnotatedType> completeReflectionOrigins;
            private final Class<?> target;

            ReflectionData(Class<?> target) {
                this.target = target;
                this.bareReflectionOrigins = getDeclaredFields(target).collect(
                        Collectors.toMap(Field::getName, Field::getType));
                this.completeReflectionOrigins = getDeclaredFields(target)
                        .collect(Collectors.toMap(Field::getName,
                                Field::getAnnotatedType));
            }

            public Map<String, Class<?>> getBareReflectionOrigins() {
                return bareReflectionOrigins;
            }

            public Map<String, AnnotatedType> getCompleteReflectionOrigins() {
                return completeReflectionOrigins;
            }

            public Class<?> getTarget() {
                return target;
            }
        }

        static class Characteristics extends Context {
            private static final ReflectionData data = new ReflectionData(
                    Sample.Characteristics.class);

            Characteristics(ScanResult source) {
                super(source, data);
            }

            Characteristics(ExtensionContext context) {
                this(SourceExtension.getSource(context));
            }
        }

        static class Default extends Context {
            private static final ReflectionData data = new ReflectionData(
                    Sample.class);

            Default(ScanResult source) {
                super(source, data);
            }

            Default(ExtensionContext context) {
                this(SourceExtension.getSource(context));
            }
        }
    }

    static class Sample {
        private @Bar DynamicDependency.@Foo Sub dynamicDependency;
        private @Bar DynamicParametrizedDependency<String>.@Foo Sub<Integer> dynamicParametrizedDependency;
        private StaticDependency.@Foo Sub staticDependency;
        private StaticParametrizedDependency.@Foo Sub<Integer> staticParametrizedDependency;
        private @Foo List<String> topLevelParametrizedDependency;

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE_USE)
        @interface Bar {
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE_USE)
        @interface Foo {
        }

        static class Characteristics {
            private Boolean aBoolean;
            private Byte aByte;
            private Character aChar;
            private Double aDouble;
            private Float aFloat;
            private Long aLong;
            private Short aShort;
            private Enum anEnum;
            private Integer anInt;
            private Date date;
            private LocalDateTime dateTime;
            private List<?> list;
            private Map<?, ?> map;
            private Object object;
            private Optional<?> optional;
            private String string;

            enum Enum {
            }
        }

        class DynamicDependency {
            class Sub {
            }
        }

        class DynamicParametrizedDependency<U> {
            class Sub<T> {
            }
        }

        static class Matches {
            private @Bar String barAnnotation;
            private @Foo String fooAnnotation;
            private List<Integer> intTypeArgument;
            private List<String> stringTypeArgument;
        }

        static class StaticDependency {
            static class Sub {
            }
        }

        static class StaticParametrizedDependency<U> {
            static class Sub<T> {
            }
        }
    }
}
