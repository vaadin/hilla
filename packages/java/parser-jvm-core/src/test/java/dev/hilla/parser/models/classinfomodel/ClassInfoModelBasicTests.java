package dev.hilla.parser.models.classinfomodel;

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
                                                ModelKind kind, TestContext context) {
        var expected = Stream.of(
                // Class Field Dependencies
                DependencyFieldStaticPublic.class,
                DependencyFieldStaticProtected.class,
                DependencyFieldStaticPrivate.class, DependencyFieldPublic.class,
                DependencyFieldProtected.class, DependencyFieldPrivate.class,

                // Class Method Dependencies
                DependencyMethodStaticPublic.class,
                DependencyMethodStaticProtected.class,
                DependencyMethodStaticPrivate.class,
                DependencyMethodPublic.class, DependencyMethodProtected.class,
                DependencyMethodPrivate.class,

                // Class Superclass Dependency
                DependencySuper.class,

                // Class Inner Class Dependencies
                Sample.DependencyInner.class,
                Sample.DependencyStaticInner.class).map(Class::getName)
                .collect(Collectors.toSet());

        var actual = model.getDependencies().stream()
                .map(ClassInfoModel::getName).collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @DisplayName("It should create correct model")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_CreateCorrectModel(ClassInfoModel model, ModelKind kind,
            TestContext context) {
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
            TestContext context) {
        var assignableReflectionClass = Child.class;
        var nonAssignableReflectionClass = Sample.DependencyStaticInner.class;

        var assignableSourceClass = context.getScanResult()
                .getClassInfo(Child.class.getName());
        var nonAssignableSourceClass = context.getScanResult()
                .getClassInfo(Sample.DependencyStaticInner.class.getName());

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
            TestContext context) {
        var sameClassName = Sample.class.getName();
        var anotherClassName = DependencySuper.class.getName();
        var sameReflectionClass = Sample.class;
        var anotherReflectionClass = DependencySuper.class;
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
            TestContext context) {
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
            TestContext context) {
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
            ModelKind kind, TestContext context) {
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
            TestContext context) {
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
            TestContext context) {
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
            TestContext context) {
        var expected = Sample.class.getSimpleName();
        var actual = model.getSimpleName();

        assertEquals(expected, actual);
    }

    @DisplayName("It should get superclass of the class")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_getSuperclass(ClassInfoModel model, ModelKind kind,
            TestContext context) {
        var expected = Sample.class.getSuperclass().getName();
        var actual = model.getSuperClass().get().getName();

        assertEquals(expected, actual);
    }

    private interface DependencyInterface {
    }

    private static final class TestContext extends BaseTestContext {
        public TestContext(ExtensionContext context) {
            super(context);
        }

        public Arguments getReflectionArguments() {
            var origin = getReflectionOrigin();
            var model = ClassInfoModel.of(origin, null);

            return Arguments.of(model, ModelKind.REFLECTION, this);
        }

        public Class<?> getReflectionOrigin() {
            return Sample.class;
        }

        public Arguments getSourceArguments() {
            var origin = getSourceOrigin();
            var model = ClassInfoModel.of(origin, mock(Model.class));

            return Arguments.of(model, ModelKind.SOURCE, this);
        }

        public ClassInfo getSourceOrigin() {
            return getScanResult().getClassInfo(Sample.class.getName());
        }
    }

    public static class ModelProvider implements ArgumentsProvider {
        public static final String testName = "{1}";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) {
            var ctx = new TestContext(context);

            return Stream.of(ctx.getReflectionArguments(),
                    ctx.getSourceArguments());
        }
    }

    private static class Child extends Sample {
    }

    private static class DependencyFieldPrivate {
    }

    private static class DependencyFieldProtected extends DependencyFieldSuper {
    }

    private static class DependencyFieldPublic {
    }

    private static class DependencyFieldStaticPrivate {
    }

    private static class DependencyFieldStaticProtected {
    }

    private static class DependencyFieldStaticPublic
            extends DependencyFieldStaticSuper {
    }

    private static class DependencyFieldStaticSuper {
    }

    private static class DependencyFieldSuper {
    }

    private static class DependencyInnerField {
    }

    private static class DependencyInnerMethod {
    }

    private static class DependencyInnerSuper {
    }

    private static class DependencyMethodPrivate {
    }

    private static class DependencyMethodProtected
            extends DependencyMethodSuper {
    }

    private static class DependencyMethodPublic {
    }

    private static class DependencyMethodStaticPrivate
            extends DependencyMethodStaticSuper {
    }

    private static class DependencyMethodStaticProtected {
    }

    private static class DependencyMethodStaticPublic {
    }

    private static class DependencyMethodStaticSuper {
    }

    private static class DependencyMethodSuper {
    }

    private static class DependencySuper extends DependencySuperSuper {
        public DependencySuperFieldPublic fieldPublic;
    }

    private static class DependencySuperFieldPublic {
    }

    private static class DependencySuperSuper {
        private DependencySuperSuperMethodPrivate methodPrivate() {
            return null;
        }
    }

    private static class DependencySuperSuperMethodPrivate {
    }

    private static class Sample extends DependencySuper
            implements DependencyInterface {
        public static DependencyFieldStaticPublic fieldStaticPublic;
        protected static DependencyFieldStaticProtected fieldStaticProtected;
        private static DependencyFieldStaticPrivate fieldStaticPrivate;
        public DependencyFieldPublic fieldPublic;
        protected DependencyFieldProtected fieldProtected;
        private DependencyFieldPrivate fieldPrivate;

        public static DependencyMethodStaticPublic methodStaticPublic() {
            return null;
        }

        protected static DependencyMethodStaticProtected methodStaticProtected() {
            return null;
        }

        private static DependencyMethodStaticPrivate methodStaticPrivate() {
            return null;
        }

        public DependencyMethodPublic methodPublic() {
            return null;
        }

        protected DependencyMethodProtected methodProtected() {
            return null;
        }

        private DependencyMethodPrivate methodPrivate() {
            return null;
        }

        public class DependencyInner {
            protected DependencyInnerMethod innerMethod() {
                return null;
            }
        }

        public static class DependencyStaticInner extends DependencyInnerSuper {
            public DependencyInnerField innerField;
        }
    }
}
