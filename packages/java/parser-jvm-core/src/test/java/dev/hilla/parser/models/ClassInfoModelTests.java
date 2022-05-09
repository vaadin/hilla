package dev.hilla.parser.models;

import static dev.hilla.parser.test.helpers.SpecializationChecker.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
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

import dev.hilla.parser.test.helpers.BaseTestContext;
import dev.hilla.parser.test.helpers.ModelKind;
import dev.hilla.parser.test.helpers.ParserExtension;
import dev.hilla.parser.test.helpers.SpecializationChecker;
import dev.hilla.parser.utils.Streams;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodTypeSignature;

@ExtendWith(ParserExtension.class)
public class ClassInfoModelTests {
    private final KindModelProvider.Checker kindChecker = new KindModelProvider.Checker();

    @DisplayName("It should collect all dependencies from the class")
    @ParameterizedTest(name = DependencyModelProvider.testName)
    @ArgumentsSource(DependencyModelProvider.class)
    public void should_CollectClassDependencies(ClassInfoModel model,
            Object origin, ModelKind kind,
            DependencyModelProvider.Context context) {
        var expected = Stream.of(SampleReferences.fieldDependencies,
                SampleReferences.methodDependencies,
                SampleReferences.parentClass, SampleReferences.innerClasses)
                .flatMap(Collection::stream).map(Class::getName)
                .collect(Collectors.toSet());

        var actual = model.getDependencies().stream()
                .map(ClassInfoModel::getName).collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @DisplayName("It should collect field dependencies of the class")
    @ParameterizedTest(name = DependencyModelProvider.testName)
    @ArgumentsSource(DependencyModelProvider.class)
    public void should_CollectClassFieldDependencies(ClassInfoModel model,
            Object origin, ModelKind kind,
            DependencyModelProvider.Context context) {
        var expected = SampleReferences.fieldDependencies.stream()
                .map(Class::getName).collect(Collectors.toSet());
        var actual = model.getFieldDependencies().stream()
                .map(ClassInfoModel::getName).collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @DisplayName("It should collect method dependencies of the class")
    @ParameterizedTest(name = DependencyModelProvider.testName)
    @ArgumentsSource(DependencyModelProvider.class)
    public void should_CollectClassMethodDependencies(ClassInfoModel model,
            Object origin, ModelKind kind,
            DependencyModelProvider.Context context) {
        var expected = SampleReferences.methodDependencies.stream()
                .map(Class::getName).collect(Collectors.toSet());
        var actual = model.getMethodDependencies().stream()
                .map(ClassInfoModel::getName).collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @DisplayName("It should collect inner class dependencies of the class")
    @ParameterizedTest(name = DependencyModelProvider.testName)
    @ArgumentsSource(DependencyModelProvider.class)
    public void should_CollectInnerClassDependencies(ClassInfoModel model,
            Object origin, ModelKind kind,
            DependencyModelProvider.Context context) {
        var expected = SampleReferences.innerClassesDependencies.stream()
                .map(Class::getName).collect(Collectors.toSet());
        var actual = model.getInnerClassDependencies().stream()
                .map(ClassInfoModel::getName).collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @DisplayName("It should create correct model")
    @ParameterizedTest(name = DependencyModelProvider.testName)
    @ArgumentsSource(DependencyModelProvider.class)
    public void should_CreateCorrectModel(ClassInfoModel model, Object origin,
            ModelKind kind, DependencyModelProvider.Context context) {
        switch (kind) {
        case REFLECTION: {
            assertEquals(origin, model.get());
            assertTrue(model.isReflection());
        }
            break;
        case SOURCE: {
            assertEquals(origin, model.get());
            assertTrue(model.isSource());
        }
            break;
        }
    }

    @DisplayName("It should detect class kind correctly")
    @ParameterizedTest(name = KindModelProvider.testName)
    @ArgumentsSource(KindModelProvider.class)
    public void should_DetectClassKind(ClassInfoModel model, Object origin,
            String[] specializations, ModelKind kind,
            KindModelProvider.Context context, String testName) {
        kindChecker.apply(model, specializations);
    }

    @Nested
    @DisplayName("In assignability scope")
    public class Assignability {
        private Class<?> assignableReflectionClass;

    }

    @DisplayName("It should check assignability from other classes")
    @ParameterizedTest(name = DependencyModelProvider.testName)
    @ArgumentsSource(DependencyModelProvider.class)
    public void should_CheckAssignability(ClassInfoModel model, Object origin,
            ModelKind kind, DependencyModelProvider.Context context) {
        var assignableReflectionClass = Dependency.SampleChild.class;
        var nonAssignableReflectionClass = Dependency.Sample.StaticInner.class;

        var assignableSourceClass = context.getScanResult()
                .getClassInfo(Dependency.SampleChild.class.getName());
        var nonAssignableSourceClass = context.getScanResult()
                .getClassInfo(Dependency.Sample.StaticInner.class.getName());

        assertTrue(model.isAssignableFrom(assignableReflectionClass));
        assertTrue(model.isAssignableFrom(assignableSourceClass));
        assertTrue(model.isAssignableFrom(
                ClassInfoModel.of(assignableReflectionClass)));
        assertTrue(model
                .isAssignableFrom(ClassInfoModel.of(assignableSourceClass)));

        assertFalse(model.isAssignableFrom(nonAssignableReflectionClass));
        assertFalse(model.isAssignableFrom(nonAssignableSourceClass));
        assertFalse(model.isAssignableFrom(
                ClassInfoModel.of(nonAssignableReflectionClass)));
        assertFalse(model
                .isAssignableFrom(ClassInfoModel.of(nonAssignableSourceClass)));

        assertTrue(ClassInfoModel.isAssignableFrom(model.getName(),
                assignableReflectionClass));
        assertTrue(ClassInfoModel.isAssignableFrom(model.getName(),
                assignableSourceClass));
        assertTrue(ClassInfoModel.isAssignableFrom(model.getName(),
                ClassInfoModel.of(assignableReflectionClass)));
        assertTrue(ClassInfoModel.isAssignableFrom(model.getName(),
                ClassInfoModel.of(assignableSourceClass)));

        assertFalse(ClassInfoModel.isAssignableFrom(model.getName(),
                nonAssignableReflectionClass));
        assertFalse(ClassInfoModel.isAssignableFrom(model.getName(),
                nonAssignableSourceClass));
        assertFalse(ClassInfoModel.isAssignableFrom(model.getName(),
                ClassInfoModel.of(nonAssignableReflectionClass)));
        assertFalse(ClassInfoModel.isAssignableFrom(model.getName(),
                ClassInfoModel.of(nonAssignableSourceClass)));

        switch (kind) {
        case REFLECTION: {
            var refOrigin = (Class<?>) origin;
            assertTrue(ClassInfoModel.isAssignableFrom(refOrigin,
                    assignableReflectionClass));
            assertTrue(ClassInfoModel.isAssignableFrom(refOrigin,
                    assignableSourceClass));
            assertTrue(ClassInfoModel.isAssignableFrom(refOrigin,
                    ClassInfoModel.of(assignableReflectionClass)));
            assertTrue(ClassInfoModel.isAssignableFrom(refOrigin,
                    ClassInfoModel.of(assignableSourceClass)));

            assertFalse(ClassInfoModel.isAssignableFrom(refOrigin,
                    nonAssignableReflectionClass));
            assertFalse(ClassInfoModel.isAssignableFrom(refOrigin,
                    nonAssignableSourceClass));
            assertFalse(ClassInfoModel.isAssignableFrom(refOrigin,
                    ClassInfoModel.of(nonAssignableReflectionClass)));
            ClassInfoModel.isAssignableFrom(refOrigin,
                    ClassInfoModel.of(nonAssignableSourceClass));
        }
            break;
        case SOURCE: {
            var sourceOrigin = (ClassInfo) origin;
            assertTrue(ClassInfoModel.isAssignableFrom(sourceOrigin,
                    assignableReflectionClass));
            assertTrue(ClassInfoModel.isAssignableFrom(sourceOrigin,
                    assignableSourceClass));
            assertTrue(ClassInfoModel.isAssignableFrom(sourceOrigin,
                    ClassInfoModel.of(assignableReflectionClass)));
            assertTrue(ClassInfoModel.isAssignableFrom(sourceOrigin,
                    ClassInfoModel.of(assignableSourceClass)));

            assertFalse(ClassInfoModel.isAssignableFrom(sourceOrigin,
                    nonAssignableReflectionClass));
            assertFalse(ClassInfoModel.isAssignableFrom(sourceOrigin,
                    nonAssignableSourceClass));
            assertFalse(ClassInfoModel.isAssignableFrom(sourceOrigin,
                    ClassInfoModel.of(nonAssignableReflectionClass)));
            ClassInfoModel.isAssignableFrom(sourceOrigin,
                    ClassInfoModel.of(nonAssignableSourceClass));
        }
            break;
        }
    }

    @DisplayName("It should be able to compare model with classes and other models")
    @ParameterizedTest(name = DependencyModelProvider.testName)
    @ArgumentsSource(DependencyModelProvider.class)
    public void should_CompareClasses(ClassInfoModel model, Object origin,
            ModelKind kind, DependencyModelProvider.Context context) {
        var sameClassName = Dependency.Sample.class.getName();
        var anotherClassName = Dependency.Parent.class.getName();
        var sameReflectionClass = Dependency.Sample.class;
        var anotherReflectionClass = Dependency.Parent.class;
        var sameSourceClass = context.getScanResult()
                .getClassInfo(sameClassName);
        var anotherSourceClass = context.getScanResult()
                .getClassInfo(anotherClassName);

        assertTrue(model.is(sameClassName));
        assertTrue(model.is(sameReflectionClass));
        assertTrue(model.is(sameSourceClass));
        assertTrue(model.is(ClassInfoModel.of(sameReflectionClass)));
        assertTrue(model.is(ClassInfoModel.of(sameSourceClass)));

        assertFalse(model.is(anotherClassName));
        assertFalse(model.is(anotherReflectionClass));
        assertFalse(model.is(anotherSourceClass));
        assertFalse(model.is(ClassInfoModel.of(anotherReflectionClass)));
        assertFalse(model.is(ClassInfoModel.of(anotherSourceClass)));

        switch (kind) {
        case REFLECTION:
            assertTrue(model.is((Class<?>) origin));
            break;
        case SOURCE:
            assertTrue(model.is((ClassInfo) origin));
            break;
        }
    }

    @DisplayName("It should get all inner classes of the class")
    @ParameterizedTest(name = DependencyModelProvider.testName)
    @ArgumentsSource(DependencyModelProvider.class)
    public void should_GetAllInnerClasses(ClassInfoModel model, Object origin,
            ModelKind kind, DependencyModelProvider.Context context) {
        var expected = Arrays
                .stream(Dependency.Sample.class.getDeclaredClasses())
                .map(Class::getName).collect(Collectors.toSet());
        var actual = model.getInnerClassesStream().map(ClassInfoModel::getName)
                .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @DisplayName("It should check equality")
    @ParameterizedTest(name = DependencyModelProvider.testName)
    @ArgumentsSource(DependencyModelProvider.class)
    public void should_GetCheckEquality(ClassInfoModel model, Object origin,
            ModelKind kind, DependencyModelProvider.Context context) {
        var reflectionModel = ClassInfoModel.of(context.getReflectionOrigin());
        var sourceModel = ClassInfoModel.of(context.getSourceOrigin());

        var otherReflectionModel = ClassInfoModel
                .of(Dependency.SampleChild.class);
        var otherSourceModel = ClassInfoModel.of(context.getScanResult()
                .getClassInfo(Dependency.SampleChild.class.getName()));

        assertEquals(model, model);
        assertEquals(model, reflectionModel);
        assertEquals(model, sourceModel);
        assertNotEquals(model, otherReflectionModel);
        assertNotEquals(model, otherSourceModel);
        assertNotEquals(model, mock(MethodInfoModel.class));
    }

    @DisplayName("It should get all fields of the class")
    @ParameterizedTest(name = DependencyModelProvider.testName)
    @ArgumentsSource(DependencyModelProvider.class)
    public void should_GetClassFields(ClassInfoModel model, Object origin,
            ModelKind kind, DependencyModelProvider.Context context) {
        var expected = Arrays
                .stream(Dependency.Sample.class.getDeclaredFields())
                .map(Field::getName).collect(Collectors.toSet());
        var actual = model.getFieldsStream().map(FieldInfoModel::getName)
                .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @DisplayName("It should get the whole inheritance chain of the class")
    @ParameterizedTest(name = DependencyModelProvider.testName)
    @ArgumentsSource(DependencyModelProvider.class)
    public void should_GetClassInheritanceChain(ClassInfoModel model,
            Object origin, ModelKind kind,
            DependencyModelProvider.Context context) {
        var expected = Stream
                .<Class<?>> iterate(Dependency.Sample.class,
                        ((Predicate<Class<?>>) Objects::nonNull)
                                .and(ClassInfoModel::isNonJDKClass),
                        Class::getSuperclass)
                .map(Class::getName).collect(Collectors.toSet());
        var actual = model.getInheritanceChainStream()
                .map(ClassInfoModel::getName).collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @DisplayName("It should get all methods of the class")
    @ParameterizedTest(name = DependencyModelProvider.testName)
    @ArgumentsSource(DependencyModelProvider.class)
    public void should_GetClassMethods(ClassInfoModel model, Object origin,
            ModelKind kind, DependencyModelProvider.Context context) {
        var expected = Arrays
                .stream(Dependency.Sample.class.getDeclaredMethods())
                .map(Method::getName).collect(Collectors.toSet());
        var actual = model.getMethodsStream().map(MethodInfoModel::getName)
                .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @DisplayName("It should get interfaces the class implements")
    @ParameterizedTest(name = DependencyModelProvider.testName)
    @ArgumentsSource(DependencyModelProvider.class)
    public void should_GetInterfaces(ClassInfoModel model, Object origin,
            ModelKind kind, DependencyModelProvider.Context context) {
        var expected = Arrays.stream(Dependency.Sample.class.getInterfaces())
                .map(Class::getName).collect(Collectors.toSet());
        var actual = model.getInterfacesStream().map(ClassInfoModel::getName)
                .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @DisplayName("It should get simple name of the class")
    @ParameterizedTest(name = DependencyModelProvider.testName)
    @ArgumentsSource(DependencyModelProvider.class)
    public void should_GetSimpleName(ClassInfoModel model, Object origin,
            ModelKind kind, DependencyModelProvider.Context context) {
        var expected = Dependency.Sample.class.getSimpleName();
        var actual = model.getSimpleName();

        assertEquals(expected, actual);
    }

    @DisplayName("It should get superclass of the class")
    @ParameterizedTest(name = DependencyModelProvider.testName)
    @ArgumentsSource(DependencyModelProvider.class)
    public void should_GetSuperclass(ClassInfoModel model, Object origin,
            ModelKind kind, DependencyModelProvider.Context context) {
        var expected = Dependency.Sample.class.getSuperclass().getName();
        var actual = model.getSuperClass().get().getName();

        assertEquals(expected, actual);
    }

    @Nested
    @DisplayName("As an AnnotatedModel")
    public class AsAnnotatedModel {
        private AnnotationInfoModel annotation;

        @BeforeEach
        public void setUp() {
            annotation = AnnotationInfoModel.of(Dependency.Sample.class
                    .getAnnotation(Dependency.Annotation.class));
        }

        @DisplayName("It should get a class annotation")
        @ParameterizedTest(name = DependencyModelProvider.testName)
        @ArgumentsSource(DependencyModelProvider.class)
        public void should_GetClassAnnotation(ClassInfoModel model,
                Object origin, ModelKind kind,
                DependencyModelProvider.Context context) {
            assertEquals(List.of(annotation), model.getAnnotations());
        }
    }

    public static final class SpecializationModelProvider
            implements ArgumentsProvider {
        public static final String testName = "{3} [{5}]";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) {
            var ctx = new SpecializationModelProvider.Context(context);

            return Streams.combine(ctx.getReflectionArguments(),
                    ctx.getSourceArguments());
        }

        public static final class Context extends BaseTestContext {
            private static final Map<String, String[]> specializations = Map
                    .ofEntries(
                            entry(Boolean.class.getName(), "isJDKClass",
                                    "isBoolean"),
                            entry(Byte.class.getName(), "isJDKClass", "isByte",
                                    "hasIntegerType"),
                            entry(Character.class.getName(), "isJDKClass",
                                    "isCharacter"),
                            entry(Double.class.getName(), "isJDKClass",
                                    "isDouble", "hasFloatType"),
                            entry(Float.class.getName(), "isJDKClass",
                                    "isFloat", "hasFloatType"),
                            entry(Integer.class.getName(), "isJDKClass",
                                    "isInteger", "hasIntegerType"),
                            entry(Long.class.getName(), "isJDKClass", "isLong",
                                    "hasIntegerType"),
                            entry(Short.class.getName(), "isJDKClass",
                                    "isShort", "hasIntegerType"),
                            entry(Void.class.getName(), "isJDKClass"));

            Context(ExtensionContext context) {
                super(context);
            }

            public Stream<Arguments> getReflectionArguments() {
                return Arrays
                        .stream(Specialization.Sample.class
                                .getDeclaredMethods())
                        .map(Method::getReturnType)
                        .map(origin -> Arguments.of(ClassInfoModel.of(origin),
                                origin, specializations.get(origin.getName()),
                                ModelKind.REFLECTION, this,
                                origin.getSimpleName()));
            }

            public Stream<Arguments> getSourceArguments() {
                return getScanResult()
                        .getClassInfo(Specialization.Sample.class.getName())
                        .getMethodInfo().stream()
                        .map(MethodInfo::getTypeSignatureOrTypeDescriptor)
                        .map(MethodTypeSignature::getResultType)
                        .map(ClassRefTypeSignature.class::cast)
                        .map(ClassRefTypeSignature::getClassInfo)
                        .map(origin -> Arguments.of(ClassInfoModel.of(origin),
                                origin, specializations.get(origin.getName()),
                                ModelKind.SOURCE, this,
                                origin.getSimpleName()));
            }
        }
    }

    static final class Dependency {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE)
        @interface Annotation {
        }

        interface Interface {
        }

        static class FieldPrivate {
        }

        static class FieldProtected extends FieldSuper {
        }

        static class FieldPublic {
        }

        static class FieldStaticPrivate {
        }

        static class FieldStaticProtected {
        }

        static class FieldStaticPublic extends FieldStaticSuper {
        }

        static class FieldStaticSuper {
        }

        static class FieldSuper {
        }

        static class GrandParent {
            private GrandParentMethodPrivate methodPrivate() {
                return null;
            }
        }

        static class GrandParentMethodPrivate {
        }

        static class InnerField {
        }

        static class InnerMethod {
        }

        static class InnerParent {
        }

        static class MethodParent {
        }

        static class MethodPrivate {
        }

        static class MethodProtected extends MethodParent {
        }

        static class MethodPublic {
        }

        static class MethodStaticParent {
        }

        static class MethodStaticPrivate extends MethodStaticParent {
        }

        static class MethodStaticProtected {
        }

        static class MethodStaticPublic {
        }

        static class Parent extends GrandParent {
            public ParentFieldPublic fieldPublic;
        }

        static class ParentFieldPublic {
        }

        @Annotation
        static class Sample extends Parent implements Interface {
            public static FieldStaticPublic fieldStaticPublic;
            protected static FieldStaticProtected fieldStaticProtected;
            private static FieldStaticPrivate fieldStaticPrivate;
            public FieldPublic fieldPublic;
            protected FieldProtected fieldProtected;
            private FieldPrivate fieldPrivate;

            public static MethodStaticPublic methodStaticPublic() {
                return null;
            }

            protected static MethodStaticProtected methodStaticProtected() {
                return null;
            }

            private static MethodStaticPrivate methodStaticPrivate() {
                return null;
            }

            public MethodPublic methodPublic() {
                return null;
            }

            protected MethodProtected methodProtected() {
                return null;
            }

            private MethodPrivate methodPrivate() {
                return null;
            }

            public class DynamicInner {
                protected InnerMethod innerMethod() {
                    return null;
                }
            }

            public static class StaticInner extends InnerParent {
                public InnerField innerField;
            }
        }

        static class SampleChild extends Sample {
        }
    }

    static final class Kind {
        enum Enum {
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE)
        @interface Annotation {
        }

        interface Interface {
        }

        static final class Final {
        }

        public static class Public {
        }

        protected static class Protected {
        }

        static abstract class Abstract {
        }

        private static class Private {
        }
    }

    private static final class SampleReferences {
        public static final Set<Class<?>> fieldDependencies = Set.of(
                Dependency.FieldStaticPublic.class,
                Dependency.FieldStaticProtected.class,
                Dependency.FieldStaticPrivate.class,
                Dependency.FieldPublic.class, Dependency.FieldProtected.class,
                Dependency.FieldPrivate.class);
        public static final Set<Class<?>> innerClasses = Set.of(
                Dependency.Sample.DynamicInner.class,
                Dependency.Sample.StaticInner.class);
        public static final Set<Class<?>> innerClassesDependencies = Set.of(
                Dependency.Sample.class, Dependency.InnerMethod.class,
                Dependency.InnerField.class, Dependency.InnerParent.class);
        public static final Set<Class<?>> methodDependencies = Set.of(
                Dependency.MethodStaticPublic.class,
                Dependency.MethodStaticProtected.class,
                Dependency.MethodStaticPrivate.class,
                Dependency.MethodPublic.class, Dependency.MethodProtected.class,
                Dependency.MethodPrivate.class);
        public static final Set<Class<?>> parentClass = Set
                .of(Dependency.Parent.class);
        public static final Set<Class<?>> parentClassDependencies = Set.of(
                Dependency.ParentFieldPublic.class,
                Dependency.GrandParent.class,
                Dependency.GrandParentMethodPrivate.class);
    }

    private static final class Specialization {
        private static class Sample {
            public Boolean getBoolean() {
                return true;
            }

            public Byte getByte() {
                return 0;
            }

            public Character getCharacter() {
                return 'a';
            }

            public Double getDouble() {
                return 0.0;
            }

            public Float getFloat() {
                return 0.0f;
            }

            public Integer getInteger() {
                return 0;
            }

            public Long getLong() {
                return 0L;
            }

            public Short getShort() {
                return 0;
            }

            public Void getVoid() {
                return null;
            }
        }
    }

    @Nested
    @DisplayName("As a SpecializedModel")
    public class AsSpecializedModel {
        private final SpecializationChecker<SpecializedModel> checker = new SpecializationChecker<>(
                SpecializedModel.class,
                SpecializedModel.class.getDeclaredMethods());

        @DisplayName("It should detect class specialization correctly")
        @ParameterizedTest(name = SpecializationModelProvider.testName)
        @ArgumentsSource(SpecializationModelProvider.class)
        public void should_DetectClassSpecialization(ClassInfoModel model,
                Object origin, String[] specializations, ModelKind kind,
                SpecializationModelProvider.Context context, String testName) {
            checker.apply(model, specializations);
        }
    }

    public static class DependencyModelProvider implements ArgumentsProvider {
        public static final String testName = "{2}";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) {
            var ctx = new Context(context);

            return Stream.of(ctx.getReflectionArguments(),
                    ctx.getSourceArguments());
        }

        public static class Context extends BaseTestContext {
            public Context(ExtensionContext context) {
                super(context);
            }

            public Arguments getReflectionArguments() {
                var origin = getReflectionOrigin();
                var model = ClassInfoModel.of(origin);

                return Arguments.of(model, origin, ModelKind.REFLECTION, this);
            }

            public Class<?> getReflectionOrigin() {
                return Dependency.Sample.class;
            }

            public Arguments getSourceArguments() {
                var origin = getSourceOrigin();
                var model = ClassInfoModel.of(origin);

                return Arguments.of(model, origin, ModelKind.SOURCE, this);
            }

            public ClassInfo getSourceOrigin() {
                return getScanResult()
                        .getClassInfo(Dependency.Sample.class.getName());
            }
        }
    }

    public static class KindModelProvider implements ArgumentsProvider {
        public static final String testName = "{3} [{5}]";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) {
            var ctx = new KindModelProvider.Context(context);

            return Streams.combine(ctx.getReflectionArguments(),
                    ctx.getSourceArguments());
        }

        private static final class Checker
                extends SpecializationChecker<ClassInfoModel> {
            private static final List<String> allowedMethods = List.of(
                    "isAbstract", "isAnnotation", "isArrayClass", "isEnum",
                    "isFinal", "isInterface", "isInterfaceOrAnnotation",
                    "isNative", "isPrivate", "isProtected", "isPublic",
                    "isStandardClass", "isStatic", "isSynthetic");

            public Checker() {
                super(ClassInfoModel.class,
                        Arrays.stream(ClassInfoModel.class.getDeclaredMethods())
                                .filter(method -> allowedMethods
                                        .contains(method.getName())));
            }
        }

        public static class Context extends BaseTestContext {
            private static final Map<Class<?>, String[]> associations = Map
                    .ofEntries(
                            entry(Kind.Abstract.class, "isAbstract",
                                    "isStandardClass", "isStatic"),
                            entry(Kind.Annotation.class, "isAnnotation",
                                    "isAbstract", "isInterfaceOrAnnotation",
                                    "isStatic"),
                            entry(Object[].class, "isAbstract", "isArrayClass",
                                    "isFinal", "isPublic", "isStandardClass"),
                            entry(Kind.Enum.class, "isEnum", "isFinal",
                                    "isStandardClass", "isStatic"),
                            entry(Kind.Final.class, "isFinal",
                                    "isStandardClass", "isStatic"),
                            entry(Kind.Interface.class, "isAbstract",
                                    "isInterface", "isInterfaceOrAnnotation",
                                    "isStatic"),
                            entry(Byte.class, "isFinal", "isPublic",
                                    "isStandardClass"),
                            entry(Kind.Private.class, "isPrivate",
                                    "isStandardClass", "isStatic"),
                            entry(Kind.Protected.class, "isProtected",
                                    "isStandardClass", "isStatic"),
                            entry(Kind.Public.class, "isPublic",
                                    "isStandardClass", "isStatic"));

            public Context(ExtensionContext context) {
                super(context);
            }

            public Stream<Arguments> getReflectionArguments() {
                return associations.entrySet().stream().map(entry -> {
                    var origin = entry.getKey();
                    var model = ClassInfoModel.of(origin);
                    var testName = origin.getSimpleName();

                    return Arguments.of(model, origin, entry.getValue(),
                            ModelKind.REFLECTION, this, testName);
                });
            }

            public Stream<Arguments> getSourceArguments() {
                return associations.entrySet().stream().map(entry -> {
                    var cls = entry.getKey();

                    var origin = cls.isArray()
                            ? ((ArrayTypeSignature) getScanResult()
                                    .getClassInfo(
                                            KindModelProvider.UnsearchableTypesSample.class
                                                    .getName())
                                    .getFieldInfo("array")
                                    .getTypeSignatureOrTypeDescriptor())
                                            .getArrayClassInfo()
                            : getScanResult().getClassInfo(cls.getName());
                    var model = ClassInfoModel.of(origin);
                    var testName = origin.getSimpleName();

                    return Arguments.of(model, origin, entry.getValue(),
                            ModelKind.SOURCE, this, testName);
                });
            }
        }

        private static class UnsearchableTypesSample {
            Object[] array;
        }
    }
}
