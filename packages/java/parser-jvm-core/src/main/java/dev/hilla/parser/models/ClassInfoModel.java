package dev.hilla.parser.models;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.utils.StreamUtils;

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
        return of(origin, null);
    }

    static ClassInfoModel of(@Nonnull ClassInfo origin, Model parent) {
        return new ClassInfoSourceModel(Objects.requireNonNull(origin), parent);
    }

    static ClassInfoModel of(@Nonnull Class<?> origin) {
        return of(origin, null);
    }

    static ClassInfoModel of(@Nonnull Class<?> origin, Model parent) {
        return new ClassInfoReflectionModel(Objects.requireNonNull(origin),
                parent);
    }

    @Override
    default Stream<ClassInfoModel> getDependenciesStream() {
        return StreamUtils.combine(getFieldDependenciesStream(),
                getMethodDependenciesStream(), getInnerClassesStream(),
                getSuperClassesStream()).distinct();
    }

    default List<ClassInfoModel> getFieldDependencies() {
        return getFieldDependenciesStream().collect(Collectors.toList());
    }

    default Stream<ClassInfoModel> getFieldDependenciesStream() {
        return getMemberDependenciesStream(ClassInfoModel::getFields,
                FieldInfoModel::getDependenciesStream);
    }

    List<FieldInfoModel> getFields();

    default Stream<FieldInfoModel> getFieldsStream() {
        return getFields().stream();
    }

    ClassInfoModelInheritanceChain getInheritanceChain();

    default List<ClassInfoModel> getInnerClassDependencies() {
        return getInnerClassDependenciesStream().collect(Collectors.toList());
    }

    default Stream<ClassInfoModel> getInnerClassDependenciesStream() {
        return getMemberDependenciesStream(ClassInfoModel::getInnerClasses,
                ClassInfoModel::getDependenciesStream);
    }

    List<ClassInfoModel> getInnerClasses();

    default Stream<ClassInfoModel> getInnerClassesStream() {
        return getInnerClasses().stream();
    }

    default <ModelMember extends Model> List<ClassInfoModel> getMemberDependencies(
            @Nonnull Function<ClassInfoModel, Collection<ModelMember>> selector,
            @Nonnull Function<ModelMember, Stream<ClassInfoModel>> dependencyExtractor) {
        return getMemberDependencies(selector,
                ClassInfoModelUtils::defaultClassInfoMemberFilter,
                dependencyExtractor);
    }

    default <ModelMember extends Model> List<ClassInfoModel> getMemberDependencies(
            @Nonnull Function<ClassInfoModel, Collection<ModelMember>> selector,
            @Nonnull Predicate<ModelMember> filter,
            @Nonnull Function<ModelMember, Stream<ClassInfoModel>> dependencyExtractor) {
        return getMemberDependenciesStream(selector, filter,
                dependencyExtractor).collect(Collectors.toList());
    }

    default <ModelMember extends Model> Stream<ClassInfoModel> getMemberDependenciesStream(
            @Nonnull Function<ClassInfoModel, Collection<ModelMember>> selector,
            @Nonnull Function<ModelMember, Stream<ClassInfoModel>> dependencyExtractor) {
        return getMemberDependenciesStream(selector,
                ClassInfoModelUtils::defaultClassInfoMemberFilter,
                dependencyExtractor);
    }

    default <ModelMember extends Model> Stream<ClassInfoModel> getMemberDependenciesStream(
            @Nonnull Function<ClassInfoModel, Collection<ModelMember>> selector,
            @Nonnull Predicate<ModelMember> filter,
            @Nonnull Function<ModelMember, Stream<ClassInfoModel>> dependencyExtractor) {
        Objects.requireNonNull(selector);
        return selector.apply(this).stream().filter(Objects::nonNull)
                .filter(Objects.requireNonNull(filter))
                .flatMap(Objects.requireNonNull(dependencyExtractor))
                .distinct();
    }

    default <Member, ModelMember extends Model> List<ModelMember> getMembers(
            @Nonnull Member[] members,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembers(members,
                ClassInfoModelUtils::defaultClassInfoMemberFilter, wrapper);
    }

    default <Member, ModelMember extends Model> List<ModelMember> getMembers(
            @Nonnull Collection<Member> members,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembers(members,
                ClassInfoModelUtils::defaultClassInfoMemberFilter, wrapper);
    }

    default <Member, ModelMember extends Model> List<ModelMember> getMembers(
            @Nonnull Stream<Member> members,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembers(members,
                ClassInfoModelUtils::defaultClassInfoMemberFilter, wrapper);
    }

    default <Member, ModelMember extends Model> List<ModelMember> getMembers(
            @Nonnull Member[] members, @Nonnull Predicate<Member> filter,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(Arrays.stream(Objects.requireNonNull(members)),
                filter, wrapper).collect(Collectors.toList());
    }

    default <Member, ModelMember extends Model> List<ModelMember> getMembers(
            @Nonnull Collection<Member> members,
            @Nonnull Predicate<Member> filter,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(Objects.requireNonNull(members).stream(),
                filter, wrapper).collect(Collectors.toList());
    }

    default <Member, ModelMember extends Model> List<ModelMember> getMembers(
            @Nonnull Stream<Member> members, @Nonnull Predicate<Member> filter,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(members, filter, wrapper)
                .collect(Collectors.toList());
    }

    default <Member, ModelMember extends Model> Stream<ModelMember> getMembersStream(
            @Nonnull Collection<Member> members,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(members,
                ClassInfoModelUtils::defaultClassInfoMemberFilter, wrapper);
    }

    default <Member, ModelMember extends Model> Stream<ModelMember> getMembersStream(
            @Nonnull Stream<Member> members,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(members,
                ClassInfoModelUtils::defaultClassInfoMemberFilter, wrapper);
    }

    default <Member, ModelMember extends Model> Stream<ModelMember> getMembersStream(
            @Nonnull Collection<Member> members,
            @Nonnull Predicate<Member> filter,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(Objects.requireNonNull(members).stream(),
                filter, wrapper);
    }

    default <Member, ModelMember extends Model> Stream<ModelMember> getMembersStream(
            @Nonnull Stream<Member> members, @Nonnull Predicate<Member> filter,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        Objects.requireNonNull(wrapper);
        return Objects.requireNonNull(members).filter(Objects::nonNull)
                .filter(Objects.requireNonNull(filter))
                .map(member -> wrapper.apply(member, this));
    }

    default List<ClassInfoModel> getMethodDependencies() {
        return getMethodDependenciesStream().collect(Collectors.toList());
    }

    default Stream<ClassInfoModel> getMethodDependenciesStream() {
        return getMemberDependenciesStream(ClassInfoModel::getMethods,
                MethodInfoModel::getDependenciesStream);
    }

    List<MethodInfoModel> getMethods();

    default Stream<MethodInfoModel> getMethodsStream() {
        return getMethods().stream();
    }

    String getSimpleName();

    Optional<ClassInfoModel> getSuperClass();

    List<ClassInfoModel> getSuperClasses();

    default Stream<ClassInfoModel> getSuperClassesStream() {
        return getSuperClasses().stream();
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
}
