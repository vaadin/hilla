package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ClassInfo;

public abstract class ClassInfoModel extends AnnotatedAbstractModel
        implements Model, NamedModel, SpecializedModel, ParameterizedModel {
    static final Comparator<ClassInfoModel> CLASS_ORDER = Comparator
            .comparing(ClassInfoModel::getSimpleName);

    private static final Class<?>[] DATE_CLASSES = { Date.class,
            LocalDate.class };
    private static final Class<?>[] DATE_TIME_CLASSES = { LocalDateTime.class,
            Instant.class, LocalTime.class };
    private List<FieldInfoModel> fields;
    private List<ClassInfoModel> innerClasses;
    private List<ClassRefSignatureModel> interfaces;
    private List<MethodInfoModel> methods;
    private PackageInfoModel pkg;
    private Optional<ClassRefSignatureModel> superClass;
    private List<TypeParameterModel> typeParameters;

    public static boolean is(Class<?> actor, String target) {
        return Objects.equals(actor.getName(), target);
    }

    @Deprecated
    public static boolean is(ClassInfo actor, String target) {
        return Objects.equals(actor.getName(), target);
    }

    public static boolean is(Class<?> actor, Class<?> target) {
        return Objects.equals(actor, target);
    }

    public static boolean is(AnnotatedType actor, AnnotatedType target) {
        return Objects.equals(actor, target);
    }

    public static boolean is(AnnotatedType actor, String target) {
        return Objects.equals(actor.getType().getTypeName(), target);
    }

    public static boolean is(Type actor, String target) {
        return Objects.equals(actor.getTypeName(), target);
    }

    @Deprecated
    public static boolean is(ClassInfo actor, Class<?> target) {
        return Objects.equals(actor.getName(), target.getName());
    }

    @Deprecated
    public static boolean is(Class<?> actor, ClassInfo target) {
        return Objects.equals(actor.getName(), target.getName());
    }

    @Deprecated
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

    @Deprecated
    public static boolean isAssignableFrom(String target, ClassInfo actor) {
        return is(actor, target) || actor.implementsInterface(target)
                || actor.extendsSuperclass(target);
    }

    public static boolean isAssignableFrom(Class<?> target, Class<?> actor) {
        return target.isAssignableFrom(actor);
    }

    @Deprecated
    public static boolean isAssignableFrom(Class<?> target, ClassInfo actor) {
        return isAssignableFrom(target.getName(), actor);
    }

    @Deprecated
    public static boolean isAssignableFrom(ClassInfo target, Class<?> actor) {
        return isAssignableFrom(target.getName(), actor);
    }

    @Deprecated
    public static boolean isAssignableFrom(ClassInfo target, ClassInfo actor) {
        return isAssignableFrom(target.getName(), actor);
    }

    public static boolean isAssignableFrom(Type target, Type actor) {
        return target instanceof Class<?> && actor instanceof Class<?>
                && isAssignableFrom((Class<?>) target, (Class<?>) actor);
    }

    public static boolean isAssignableFrom(AnnotatedType target,
            AnnotatedType actor) {
        return isAssignableFrom(target.getType(), actor.getType());
    }

    public static boolean isAssignableFrom(String target, Type actor) {
        return actor instanceof Class<?>
                && isAssignableFrom(target, (Class<?>) actor);
    }

    public static boolean isAssignableFrom(String target, AnnotatedType actor) {
        return isAssignableFrom(target, actor.getType());
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

    @Deprecated
    public static boolean isAssignableFrom(ClassInfo target,
            ClassInfoModel actor) {
        var _actor = actor.get();

        return _actor instanceof ClassInfo
                ? isAssignableFrom(target, (ClassInfo) _actor)
                : isAssignableFrom(target, (Class<?>) _actor);
    }

    public static boolean isJDKClass(AnnotatedType cls) {
        return isJDKClass(cls.getType().getTypeName());
    }

    public static boolean isJDKClass(Type cls) {
        return isJDKClass(cls.getTypeName());
    }

    @Deprecated
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

    @Deprecated
    public static boolean isNonJDKClass(ClassInfo cls) {
        return !isJDKClass(cls);
    }

    public static boolean isNonJDKClass(Class<?> cls) {
        return !isJDKClass(cls);
    }

    public static boolean isNonJDKClass(Type cls) {
        return !isJDKClass(cls);
    }

    public static boolean isNonJDKClass(AnnotatedType cls) {
        return !isJDKClass(cls);
    }

    @Deprecated
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

    public final Optional<ClassRefSignatureModel> getSuperClass() {
        if (superClass == null) {
            superClass = Optional.ofNullable(prepareSuperClass());
        }

        return superClass;
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
    public Class<ClassInfoModel> getCommonModelClass() {
        return ClassInfoModel.class;
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
        var chain = new ArrayList<ClassInfoModel>();

        var current = this;

        while (current != null && isNonJDKClass(current.getName())) {
            chain.add(current);
            current = current.getSuperClass()
                    .map(ClassRefSignatureModel::getClassInfo).orElse(null);
        }

        return chain;
    }

    public Stream<ClassInfoModel> getInheritanceChainStream() {
        return getInheritanceChain().stream();
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

    public List<ClassRefSignatureModel> getInterfaces() {
        if (interfaces == null) {
            interfaces = prepareInterfaces();
        }

        return interfaces;
    }

    public Stream<ClassRefSignatureModel> getInterfacesStream() {
        return getInterfaces().stream();
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

    public Stream<ClassRefSignatureModel> getSuperClassStream() {
        return getSuperClass().stream();
    }

    @Override
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

    public boolean is(Type cls) {
        return cls instanceof Class<?> && is((Class<?>) cls);
    }

    public boolean is(AnnotatedType cls) {
        return is(cls.getType());
    }

    @Deprecated
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

    @Deprecated
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

    public boolean isAssignableFrom(AnnotatedType cls) {
        return isAssignableFrom(cls.getClass());
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

    protected abstract List<ClassInfoModel> prepareInnerClasses();

    protected abstract List<ClassRefSignatureModel> prepareInterfaces();

    protected abstract List<MethodInfoModel> prepareMethods();

    protected abstract PackageInfoModel preparePackage();

    protected abstract ClassRefSignatureModel prepareSuperClass();

    protected abstract List<TypeParameterModel> prepareTypeParameters();
}
