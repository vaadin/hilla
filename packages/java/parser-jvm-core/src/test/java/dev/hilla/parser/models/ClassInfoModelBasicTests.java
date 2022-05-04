package dev.hilla.parser.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.Model;
import dev.hilla.parser.test.helpers.BaseTestContext;
import dev.hilla.parser.test.helpers.ModelKind;
import dev.hilla.parser.test.helpers.ParserExtension;
import dev.hilla.parser.utils.Predicates;

import io.github.classgraph.ClassInfo;

@ExtendWith(ParserExtension.class)
public class ClassInfoModelBasicTests {
    @DisplayName("It should collect all dependencies from the class")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_CollectClassDependencies(ClassInfoModel model,
            ModelKind kind, ModelProvider.Context context) {
        var expected = Stream.of(
                // Class Field Dependencies
                Dependency.FieldStaticPublic.class,
                Dependency.FieldStaticProtected.class,
                Dependency.FieldStaticPrivate.class,
                Dependency.FieldPublic.class, Dependency.FieldProtected.class,
                Dependency.FieldPrivate.class,

                // Class Method Dependencies
                Dependency.MethodStaticPublic.class,
                Dependency.MethodStaticProtected.class,
                Dependency.MethodStaticPrivate.class,
                Dependency.MethodPublic.class, Dependency.MethodProtected.class,
                Dependency.MethodPrivate.class,

                // Class Superclass Dependency
                Dependency.Parent.class,

                // Class Inner Class Dependencies
                Sample.DependencyInner.class, Sample.StaticInner.class)
                .map(Class::getName).collect(Collectors.toSet());

        var actual = model.getDependencies().stream()
                .map(ClassInfoModel::getName).collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @DisplayName("It should create correct model")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_CreateCorrectModel(ClassInfoModel model, ModelKind kind,
            ModelProvider.Context context) {
        switch (kind) {
        case REFLECTION: {
            var origin = context.getReflectionOrigin();
            assertEquals(origin, model.get());
            assertTrue(model.isReflection());
        }
            break;
        case SOURCE: {
            var origin = context.getSourceOrigin();
            assertEquals(origin, model.get());
            assertTrue(model.isSource());
        }
            break;
        }
    }

    @DisplayName("It should check if it is assignable from other classes")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_checkAssignability(ClassInfoModel model, ModelKind kind,
            ModelProvider.Context context) {
        var assignableReflectionClass = SampleChild.class;
        var nonAssignableReflectionClass = Sample.StaticInner.class;

        var assignableSourceClass = context.getScanResult()
                .getClassInfo(SampleChild.class.getName());
        var nonAssignableSourceClass = context.getScanResult()
                .getClassInfo(Sample.StaticInner.class.getName());

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
    }

    @DisplayName("It should be able to compare model with classes and other models")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_compareClasses(ClassInfoModel model, ModelKind kind,
            ModelProvider.Context context) {
        var sameClassName = Sample.class.getName();
        var anotherClassName = Dependency.Parent.class.getName();
        var sameReflectionClass = Sample.class;
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
            assertTrue(model.is(context.getReflectionOrigin()));
            break;
        case SOURCE:
            assertTrue(model.is(context.getSourceOrigin()));
            break;
        }
    }

    @DisplayName("It should get all inner classes of the class")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_getAllInnerClasses(ClassInfoModel model, ModelKind kind,
            ModelProvider.Context context) {
        var expected = Arrays.stream(Sample.class.getDeclaredClasses())
                .map(Class::getName).collect(Collectors.toSet());
        var actual = model.getInnerClassesStream().map(ClassInfoModel::getName)
                .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @DisplayName("It should get all fields of the class")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_getClassFields(ClassInfoModel model, ModelKind kind,
            ModelProvider.Context context) {
        var expected = Arrays.stream(Sample.class.getDeclaredFields())
                .map(Field::getName).collect(Collectors.toSet());
        var actual = model.getFieldsStream().map(FieldInfoModel::getName)
                .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @DisplayName("It should get the whole inheritance chain of the class")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_getClassInheritanceChain(ClassInfoModel model,
            ModelKind kind, ModelProvider.Context context) {
        var expected = Stream
                .<Class<?>> iterate(Sample.class,
                        Predicates.and(Objects::nonNull,
                                ClassInfoModel::isNonJDKClass),
                        Class::getSuperclass)
                .map(Class::getName).collect(Collectors.toSet());
        var actual = model.getInheritanceChainStream()
                .map(ClassInfoModel::getName).collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @DisplayName("It should get all methods of the class")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_getClassMethods(ClassInfoModel model, ModelKind kind,
            ModelProvider.Context context) {
        var expected = Arrays.stream(Sample.class.getDeclaredMethods())
                .map(Method::getName).collect(Collectors.toSet());
        var actual = model.getMethodsStream().map(MethodInfoModel::getName)
                .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @DisplayName("It should get interfaces the class implements")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_getInterfaces(ClassInfoModel model, ModelKind kind,
            ModelProvider.Context context) {
        var expected = Arrays.stream(Sample.class.getInterfaces())
                .map(Class::getName).collect(Collectors.toSet());
        var actual = model.getInterfacesStream().map(ClassInfoModel::getName)
                .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @DisplayName("It should get simple name of the class")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_getSimpleName(ClassInfoModel model, ModelKind kind,
            ModelProvider.Context context) {
        var expected = Sample.class.getSimpleName();
        var actual = model.getSimpleName();

        assertEquals(expected, actual);
    }

    @DisplayName("It should get superclass of the class")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_getSuperclass(ClassInfoModel model, ModelKind kind,
            ModelProvider.Context context) {
        var expected = Sample.class.getSuperclass().getName();
        var actual = model.getSuperClass().get().getName();

        assertEquals(expected, actual);
    }

    private static final class Dependency {
        private interface Interface {
        }

        private static class FieldPrivate {
        }

        private static class FieldProtected extends FieldSuper {
        }

        private static class FieldPublic {
        }

        private static class FieldStaticPrivate {
        }

        private static class FieldStaticProtected {
        }

        private static class FieldStaticPublic extends FieldStaticSuper {
        }

        private static class FieldStaticSuper {
        }

        private static class FieldSuper {
        }

        private static class GrandParent {
            private GrandParentMethodPrivate methodPrivate() {
                return null;
            }
        }

        private static class GrandParentMethodPrivate {
        }

        private static class InnerField {
        }

        private static class InnerMethod {
        }

        private static class InnerParent {
        }

        private static class MethodParent {
        }

        private static class MethodPrivate {
        }

        private static class MethodProtected extends MethodParent {
        }

        private static class MethodPublic {
        }

        private static class MethodStaticParent {
        }

        private static class MethodStaticPrivate extends MethodStaticParent {
        }

        private static class MethodStaticProtected {
        }

        private static class MethodStaticPublic {
        }

        private static class Parent extends GrandParent {
            public ParentFieldPublic fieldPublic;
        }

        private static class ParentFieldPublic {
        }
    }

    public static class ModelProvider implements ArgumentsProvider {
        public static final String testName = "{1}";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) {
            var ctx = new Context(context);

            return Stream.of(ctx.getReflectionArguments(),
                    ctx.getSourceArguments());
        }

        public static class Context extends BaseTestContext {
            private final Class<?> reflectionOrigin;
            private final ClassInfo sourceOrigin;

            public Context(ExtensionContext context) {
                super(context);
                sourceOrigin = getScanResult()
                        .getClassInfo(Sample.class.getName());
                reflectionOrigin = Sample.class;
            }

            public Arguments getReflectionArguments() {
                var model = ClassInfoModel.of(reflectionOrigin, null);

                return Arguments.of(model, ModelKind.REFLECTION, this);
            }

            public Class<?> getReflectionOrigin() {
                return reflectionOrigin;
            }

            public Arguments getSourceArguments() {
                var model = ClassInfoModel.of(sourceOrigin, mock(Model.class));

                return Arguments.of(model, ModelKind.SOURCE, this);
            }

            public ClassInfo getSourceOrigin() {
                return sourceOrigin;
            }
        }
    }

    private static class Sample extends Dependency.Parent
            implements Dependency.Interface {
        public static Dependency.FieldStaticPublic fieldStaticPublic;
        protected static Dependency.FieldStaticProtected fieldStaticProtected;
        private static Dependency.FieldStaticPrivate fieldStaticPrivate;
        public Dependency.FieldPublic fieldPublic;
        protected Dependency.FieldProtected fieldProtected;
        private Dependency.FieldPrivate fieldPrivate;

        public static Dependency.MethodStaticPublic methodStaticPublic() {
            return null;
        }

        protected static Dependency.MethodStaticProtected methodStaticProtected() {
            return null;
        }

        private static Dependency.MethodStaticPrivate methodStaticPrivate() {
            return null;
        }

        public Dependency.MethodPublic methodPublic() {
            return null;
        }

        protected Dependency.MethodProtected methodProtected() {
            return null;
        }

        private Dependency.MethodPrivate methodPrivate() {
            return null;
        }

        public class DependencyInner {
            protected Dependency.InnerMethod innerMethod() {
                return null;
            }
        }

        public static class StaticInner extends Dependency.InnerParent {
            public Dependency.InnerField innerField;
        }
    }

    private static class SampleChild extends Sample {
    }
}
