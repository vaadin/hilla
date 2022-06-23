package dev.hilla.parser.models;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.utils.Streams;

import io.github.classgraph.ClassInfo;

public interface ClassInfoModel
        extends Model, NamedModel, AnnotatedModel, SpecializedModel {
    static boolean is(Class<?> actor, String target) {
        return Objects.equals(actor.getName(), target);
    }

    static boolean is(ClassInfo actor, String target) {
        return Objects.equals(actor.getName(), target);
    }

    static boolean is(Class<?> actor, Class<?> target) {
        return Objects.equals(actor, target);
    }

    static boolean is(ClassInfo actor, Class<?> target) {
        return Objects.equals(actor.getName(), target.getName());
    }

    static boolean is(Class<?> actor, ClassInfo target) {
        return Objects.equals(actor.getName(), target.getName());
    }

    static boolean is(ClassInfo actor, ClassInfo target) {
        return Objects.equals(actor, target);
    }

    static boolean isAssignableFrom(String target, Class<?> actor) {
        while (actor != null) {
            if (Objects.equals(target, actor.getName())) {
                return true;
            }

            actor = actor.getSuperclass();
        }

        return false;
    }

    static boolean isAssignableFrom(String target, ClassInfo actor) {
        return is(actor, target) || actor.implementsInterface(target)
                || actor.extendsSuperclass(target);
    }

    static boolean isAssignableFrom(Class<?> target, Class<?> actor) {
        return target.isAssignableFrom(actor);
    }

    static boolean isAssignableFrom(Class<?> target, ClassInfo actor) {
        return isAssignableFrom(target.getName(), actor);
    }

    static boolean isAssignableFrom(ClassInfo target, Class<?> actor) {
        return isAssignableFrom(target.getName(), actor);
    }

    static boolean isAssignableFrom(ClassInfo target, ClassInfo actor) {
        return isAssignableFrom(target.getName(), actor);
    }

    static boolean isAssignableFrom(String target, ClassInfoModel actor) {
        var _actor = actor.get();

        return _actor instanceof ClassInfo
                ? isAssignableFrom(target, (ClassInfo) _actor)
                : isAssignableFrom(target, (Class<?>) _actor);
    }

    static boolean isAssignableFrom(Class<?> target, ClassInfoModel actor) {
        var _actor = actor.get();

        return _actor instanceof ClassInfo
                ? isAssignableFrom(target, (ClassInfo) _actor)
                : isAssignableFrom(target, (Class<?>) _actor);
    }

    static boolean isAssignableFrom(ClassInfo target, ClassInfoModel actor) {
        var _actor = actor.get();

        return _actor instanceof ClassInfo
                ? isAssignableFrom(target, (ClassInfo) _actor)
                : isAssignableFrom(target, (Class<?>) _actor);
    }

    static boolean isJDKClass(ClassInfo cls) {
        return isJDKClass(cls.getName());
    }

    static boolean isJDKClass(String name) {
        return name.startsWith("java") || name.startsWith("com.sun")
                || name.startsWith("sun") || name.startsWith("oracle")
                || name.startsWith("org.xml") || name.startsWith("com.oracle");
    }

    static boolean isJDKClass(Class<?> cls) {
        return isJDKClass(cls.getName());
    }

    static boolean isNonJDKClass(String name) {
        return !isJDKClass(name);
    }

    static boolean isNonJDKClass(ClassInfo cls) {
        return !isJDKClass(cls);
    }

    static boolean isNonJDKClass(Class<?> cls) {
        return !isJDKClass(cls);
    }

    static ClassInfoModel of(@Nonnull ClassInfo origin) {
        return new ClassInfoSourceModel(Objects.requireNonNull(origin));
    }

    static ClassInfoModel of(@Nonnull Class<?> origin) {
        return new ClassInfoReflectionModel(Objects.requireNonNull(origin));
    }

    @Override
    default Stream<ClassInfoModel> getDependenciesStream() {
        return Streams.combine(getFieldDependenciesStream(),
                getMethodDependenciesStream(), getInnerClassesStream(),
                getSuperClassStream()).distinct();
    }

    default Set<ClassInfoModel> getFieldDependencies() {
        return getFieldDependenciesStream().collect(Collectors.toSet());
    }

    default Stream<ClassInfoModel> getFieldDependenciesStream() {
        return getFieldsStream().flatMap(FieldInfoModel::getDependenciesStream)
                .distinct();
    }

    List<FieldInfoModel> getFields();

    default Stream<FieldInfoModel> getFieldsStream() {
        return getFields().stream();
    }

    List<ClassInfoModel> getInheritanceChain();

    default Stream<ClassInfoModel> getInheritanceChainStream() {
        return getInheritanceChain().stream();
    }

    default Set<ClassInfoModel> getInnerClassDependencies() {
        return getInnerClassDependenciesStream().collect(Collectors.toSet());
    }

    default Stream<ClassInfoModel> getInnerClassDependenciesStream() {
        return getInnerClassesStream()
                .flatMap(ClassInfoModel::getDependenciesStream).distinct();
    }

    List<ClassInfoModel> getInnerClasses();

    default Stream<ClassInfoModel> getInnerClassesStream() {
        return getInnerClasses().stream();
    }

    List<ClassInfoModel> getInterfaces();

    default Stream<ClassInfoModel> getInterfacesStream() {
        return getInterfaces().stream();
    }

    default Set<ClassInfoModel> getMethodDependencies() {
        return getMethodDependenciesStream().collect(Collectors.toSet());
    }

    default Stream<ClassInfoModel> getMethodDependenciesStream() {
        return getMethodsStream()
                .flatMap(MethodInfoModel::getDependenciesStream).distinct();
    }

    List<MethodInfoModel> getMethods();

    default Stream<MethodInfoModel> getMethodsStream() {
        return getMethods().stream();
    }

    String getSimpleName();

    Optional<ClassInfoModel> getSuperClass();

    default Stream<ClassInfoModel> getSuperClassStream() {
        return getSuperClass().stream();
    }

    default boolean is(String name) {
        var origin = get();

        return origin instanceof ClassInfo ? is((ClassInfo) origin, name)
                : is((Class<?>) origin, name);
    }

    default boolean is(Class<?> cls) {
        var origin = get();

        return origin instanceof ClassInfo ? is((ClassInfo) origin, cls)
                : is((Class<?>) origin, cls);
    }

    default boolean is(ClassInfo cls) {
        var origin = get();

        return origin instanceof ClassInfo ? is((ClassInfo) origin, cls)
                : is((Class<?>) origin, cls);
    }

    default boolean is(ClassInfoModel model) {
        var cls = model.get();

        return cls instanceof ClassInfo ? is((ClassInfo) cls)
                : is((Class<?>) cls);
    }

    boolean isAbstract();

    boolean isAnnotation();

    boolean isArrayClass();

    default boolean isAssignableFrom(ClassInfoModel model) {
        var _model = model.get();

        return _model instanceof ClassInfo
                ? isAssignableFrom((ClassInfo) _model)
                : isAssignableFrom((Class<?>) _model);
    }

    default boolean isAssignableFrom(ClassInfo cls) {
        var origin = get();

        return origin instanceof ClassInfo
                ? isAssignableFrom((ClassInfo) origin, cls)
                : isAssignableFrom((Class<?>) origin, cls);
    }

    default boolean isAssignableFrom(Class<?> cls) {
        var origin = get();

        return origin instanceof ClassInfo
                ? isAssignableFrom((ClassInfo) origin, cls)
                : isAssignableFrom((Class<?>) origin, cls);
    }

    boolean isEnum();

    boolean isFinal();

    boolean isInterface();

    boolean isInterfaceOrAnnotation();

    default boolean isNative() {
        return false;
    }

    boolean isPrivate();

    boolean isProtected();

    boolean isPublic();

    boolean isStandardClass();

    boolean isStatic();

    boolean isSynthetic();

    @FunctionalInterface
    interface ArrayFn<T> extends Function<Object, T[]> {

    }

    @FunctionalInterface
    interface CollectionFn<T> extends Function<Object, List<T>> {
    }
}
