package dev.hilla.parser.models;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.utils.Streams;

import io.github.classgraph.ClassInfo;

public abstract class ClassInfoModel extends AnnotatedAbstractModel
        implements Model, NamedModel, SpecializedModel {
    private static final Class<?>[] DATE_CLASSES = { Date.class,
            LocalDate.class };
    private static final Class<?>[] DATE_TIME_CLASSES = { LocalDateTime.class,
            Instant.class, LocalTime.class };
    private List<ClassInfoModel> chain;
    private List<FieldInfoModel> fields;
    private List<ClassInfoModel> innerClasses;
    private List<ClassInfoModel> interfaces;
    private List<MethodInfoModel> methods;
    private PackageInfoModel pkg;
    private Optional<ClassInfoModel> superClass;
    private List<TypeParameterModel> typeParameters;

    public static boolean is(Class<?> actor, String target) {
        return Objects.equals(actor.getName(), target);
    }

    public static boolean is(ClassInfo actor, String target) {
        return Objects.equals(actor.getName(), target);
    }

    public static boolean is(Class<?> actor, Class<?> target) {
        return Objects.equals(actor, target);
    }

    public static boolean is(ClassInfo actor, Class<?> target) {
        return Objects.equals(actor.getName(), target.getName());
    }

    public static boolean is(Class<?> actor, ClassInfo target) {
        return Objects.equals(actor.getName(), target.getName());
    }

    public static boolean is(ClassInfo actor, ClassInfo target) {
        return Objects.equals(actor, target);
    }

    public static boolean isAssignableFrom(String target, Class<?> actor) {
        while (actor != null) {
            if (Objects.equals(target, actor.getName())) {
                return true;
            }

            actor = actor.getSuperclass();
        }

        return false;
    }

    public static boolean isAssignableFrom(String target, ClassInfo actor) {
        return is(actor, target) || actor.implementsInterface(target)
                || actor.extendsSuperclass(target);
    }

    public static boolean isAssignableFrom(Class<?> target, Class<?> actor) {
        return target.isAssignableFrom(actor);
    }

    public static boolean isAssignableFrom(Class<?> target, ClassInfo actor) {
        return isAssignableFrom(target.getName(), actor);
    }

    public static boolean isAssignableFrom(ClassInfo target, Class<?> actor) {
        return isAssignableFrom(target.getName(), actor);
    }

    public static boolean isAssignableFrom(ClassInfo target, ClassInfo actor) {
        return isAssignableFrom(target.getName(), actor);
    }

    public static boolean isAssignableFrom(String target,
            ClassInfoModel actor) {
        var _actor = actor.get();

        return _actor instanceof ClassInfo
                ? isAssignableFrom(target, (ClassInfo) _actor)
                : isAssignableFrom(target, (Class<?>) _actor);
    }

    public static boolean isAssignableFrom(Class<?> target,
            ClassInfoModel actor) {
        var _actor = actor.get();

        return _actor instanceof ClassInfo
                ? isAssignableFrom(target, (ClassInfo) _actor)
                : isAssignableFrom(target, (Class<?>) _actor);
    }

    public static boolean isAssignableFrom(ClassInfo target,
            ClassInfoModel actor) {
        var _actor = actor.get();

        return _actor instanceof ClassInfo
                ? isAssignableFrom(target, (ClassInfo) _actor)
                : isAssignableFrom(target, (Class<?>) _actor);
    }

    public static boolean isJDKClass(ClassInfo cls) {
        return isJDKClass(cls.getName());
    }

    public static boolean isJDKClass(String name) {
        return name.startsWith("java") || name.startsWith("com.sun")
                || name.startsWith("sun") || name.startsWith("oracle")
                || name.startsWith("org.xml") || name.startsWith("com.oracle");
    }

    public static boolean isJDKClass(Class<?> cls) {
        return isJDKClass(cls.getName());
    }

    public static boolean isNonJDKClass(String name) {
        return !isJDKClass(name);
    }

    public static boolean isNonJDKClass(ClassInfo cls) {
        return !isJDKClass(cls);
    }

    public static boolean isNonJDKClass(Class<?> cls) {
        return !isJDKClass(cls);
    }

    public static ClassInfoModel of(@Nonnull ClassInfo origin) {
        return new ClassInfoSourceModel(Objects.requireNonNull(origin));
    }

    public static ClassInfoModel of(@Nonnull Class<?> origin) {
        return new ClassInfoReflectionModel(Objects.requireNonNull(origin));
    }

    protected static <T> boolean isDateAssignable(T actor,
            BiPredicate<Class<?>, T> predicate) {
        return Arrays.stream(DATE_CLASSES)
                .anyMatch(cls -> predicate.test(cls, actor));
    }

    protected static <T> boolean isDateTimeAssignable(T actor,
            BiPredicate<Class<?>, T> predicate) {
        return Arrays.stream(DATE_TIME_CLASSES)
                .anyMatch(cls -> predicate.test(cls, actor));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ClassInfoModel)) {
            return false;
        }

        var other = (ClassInfoModel) obj;

        return getName().equals(other.getName());
    }

    @Override
    public Stream<ClassInfoModel> getDependenciesStream() {
        return Streams
                .combine(getFieldDependenciesStream(),
                        getMethodDependenciesStream(), getInnerClassesStream(),
                        getSuperClassStream(), getInterfacesStream(),
                        getInterfaceDependenciesStream(),
                        getTypeParameterDependenciesStream())
                .filter(ClassInfoModel::isNonJDKClass).distinct();
    }

    public Set<ClassInfoModel> getFieldDependencies() {
        return getFieldDependenciesStream().collect(Collectors.toSet());
    }

    public Stream<ClassInfoModel> getFieldDependenciesStream() {
        return getFieldsStream().flatMap(FieldInfoModel::getDependenciesStream)
                .distinct();
    }

    public List<FieldInfoModel> getFields() {
        if (fields == null) {
            fields = prepareFields();
        }

        return fields;
    }

    public Stream<FieldInfoModel> getFieldsStream() {
        return getFields().stream();
    }

    public List<ClassInfoModel> getInheritanceChain() {
        if (chain == null) {
            chain = prepareInheritanceChain();
        }

        return chain;
    }

    public Stream<ClassInfoModel> getInheritanceChainStream() {
        return getInheritanceChain().stream();
    }

    public Set<ClassInfoModel> getInnerClassDependencies() {
        return getInnerClassDependenciesStream().collect(Collectors.toSet());
    }

    public Stream<ClassInfoModel> getInnerClassDependenciesStream() {
        return getInnerClassesStream()
                .flatMap(ClassInfoModel::getDependenciesStream).distinct();
    }

    public List<ClassInfoModel> getInnerClasses() {
        if (innerClasses == null) {
            innerClasses = prepareInnerClasses();
        }

        return innerClasses;
    }

    public Stream<ClassInfoModel> getInnerClassesStream() {
        return getInnerClasses().stream();
    }

    public Set<ClassInfoModel> getInterfaceDependencies() {
        return getInterfaceDependenciesStream().collect(Collectors.toSet());
    }

    public Stream<ClassInfoModel> getInterfaceDependenciesStream() {
        return getInterfacesStream()
                .flatMap(ClassInfoModel::getDependenciesStream).distinct();
    }

    public List<ClassInfoModel> getInterfaces() {
        if (interfaces == null) {
            interfaces = prepareInterfaces();
        }

        return interfaces;
    }

    public Stream<ClassInfoModel> getInterfacesStream() {
        return getInterfaces().stream();
    }

    public Set<ClassInfoModel> getMethodDependencies() {
        return getMethodDependenciesStream().collect(Collectors.toSet());
    }

    public Stream<ClassInfoModel> getMethodDependenciesStream() {
        return getMethodsStream()
                .flatMap(MethodInfoModel::getDependenciesStream).distinct();
    }

    public List<MethodInfoModel> getMethods() {
        if (methods == null) {
            methods = prepareMethods();
        }

        return methods;
    }

    public Stream<MethodInfoModel> getMethodsStream() {
        return getMethods().stream();
    }

    public PackageInfoModel getPackage() {
        if (pkg == null) {
            pkg = preparePackage();
        }

        return pkg;
    }

    public abstract String getSimpleName();

    public Optional<ClassInfoModel> getSuperClass() {
        if (superClass == null) {
            superClass = Optional.ofNullable(prepareSuperClass());
        }

        return superClass;
    }

    public Stream<ClassInfoModel> getSuperClassStream() {
        return getSuperClass().stream();
    }

    public Stream<ClassInfoModel> getTypeParameterDependenciesStream() {
        return getTypeParameterStream()
                .flatMap(TypeParameterModel::getDependenciesStream).distinct();
    }

    public Stream<TypeParameterModel> getTypeParameterStream() {
        return getTypeParameters().stream();
    }

    public List<TypeParameterModel> getTypeParameters() {
        if (typeParameters == null) {
            typeParameters = prepareTypeParameters();
        }

        return typeParameters;
    }

    public int hashCode() {
        return 3 + getName().hashCode();
    }

    public boolean is(String name) {
        var origin = get();

        return origin instanceof ClassInfo ? is((ClassInfo) origin, name)
                : is((Class<?>) origin, name);
    }

    public boolean is(Class<?> cls) {
        var origin = get();

        return origin instanceof ClassInfo ? is((ClassInfo) origin, cls)
                : is((Class<?>) origin, cls);
    }

    public boolean is(ClassInfo cls) {
        var origin = get();

        return origin instanceof ClassInfo ? is((ClassInfo) origin, cls)
                : is((Class<?>) origin, cls);
    }

    public boolean is(ClassInfoModel model) {
        var cls = model.get();

        return cls instanceof ClassInfo ? is((ClassInfo) cls)
                : is((Class<?>) cls);
    }

    public abstract boolean isAbstract();

    public abstract boolean isAnnotation();

    public abstract boolean isArrayClass();

    public boolean isAssignableFrom(ClassInfoModel model) {
        var _model = model.get();

        return _model instanceof ClassInfo
                ? isAssignableFrom((ClassInfo) _model)
                : isAssignableFrom((Class<?>) _model);
    }

    public boolean isAssignableFrom(ClassInfo cls) {
        var origin = get();

        return origin instanceof ClassInfo
                ? isAssignableFrom((ClassInfo) origin, cls)
                : isAssignableFrom((Class<?>) origin, cls);
    }

    public boolean isAssignableFrom(Class<?> cls) {
        var origin = get();

        return origin instanceof ClassInfo
                ? isAssignableFrom((ClassInfo) origin, cls)
                : isAssignableFrom((Class<?>) origin, cls);
    }

    public abstract boolean isEnum();

    public abstract boolean isFinal();

    public abstract boolean isInterface();

    public abstract boolean isInterfaceOrAnnotation();

    public boolean isNative() {
        return false;
    }

    public abstract boolean isPrivate();

    public abstract boolean isProtected();

    public abstract boolean isPublic();

    public abstract boolean isStandardClass();

    public abstract boolean isStatic();

    public abstract boolean isSynthetic();

    protected abstract List<FieldInfoModel> prepareFields();

    protected abstract List<ClassInfoModel> prepareInheritanceChain();

    protected abstract List<ClassInfoModel> prepareInnerClasses();

    protected abstract List<ClassInfoModel> prepareInterfaces();

    protected abstract List<MethodInfoModel> prepareMethods();

    protected abstract PackageInfoModel preparePackage();

    protected abstract ClassInfoModel prepareSuperClass();

    protected abstract List<TypeParameterModel> prepareTypeParameters();
}
