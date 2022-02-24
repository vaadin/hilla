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
        return getFieldDependenciesStream().collect(Collectors.toList());
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

    boolean isAbstract();

    boolean isAnnotation();

    boolean isArrayClass();

    boolean isEnum();

    boolean isFinal();

    boolean isInterface();

    boolean isInterfaceOrAnnotation();

    boolean isPrivate();

    boolean isProtected();

    boolean isPublic();

    boolean isStandardClass();

    boolean isStatic();

    boolean isSynthetic();
}
