package dev.hilla.parser.models;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ClassInfo;

public interface ClassInfoModel extends Model, Dependable {
    static ClassInfoModel of(@Nonnull ClassInfo origin) {
        return of(origin, null);
    }

    static ClassInfoModel of(@Nonnull ClassInfo origin, Model parent) {
        return new ClassInfoSourceModel(Objects.requireNonNull(origin), parent);
    }

    static ClassInfoModel of(@Nonnull Type origin) {
        return of(origin, null);
    }

    static ClassInfoModel of(@Nonnull Type origin, Model parent) {
        return new ClassInfoReflectionModel(Objects.requireNonNull(origin),
                parent);
    }

    Collection<AnnotationInfoModel> getAnnotations();

    default Stream<AnnotationInfoModel> getAnnotationsStream() {
        return getAnnotations().stream();
    }

    default Collection<ClassInfoModel> getFieldDependencies() {
        return getFieldDependenciesStream().collect(Collectors.toSet());
    }

    default Stream<ClassInfoModel> getFieldDependenciesStream() {
        return getMemberDependenciesStream(ClassInfoModel::getFields,
                FieldInfoModel::getDependenciesStream);
    }

    Collection<FieldInfoModel> getFields();

    default Stream<FieldInfoModel> getFieldsStream() {
        return getFields().stream();
    }

    ClassInfoModelInheritanceChain getInheritanceChain();

    default Collection<ClassInfoModel> getInnerClassDependencies() {
        return getFieldDependenciesStream().collect(Collectors.toSet());
    }

    default Stream<ClassInfoModel> getInnerClassDependenciesStream() {
        return getMemberDependenciesStream(ClassInfoModel::getInnerClasses,
                ClassInfoModel::getDependenciesStream);
    }

    Collection<ClassInfoModel> getInnerClasses();

    default Stream<ClassInfoModel> getInnerClassesStream() {
        return getInnerClasses().stream();
    }

    default <ModelMember extends Model> Collection<ClassInfoModel> getMemberDependencies(
            @Nonnull Function<ClassInfoModel, Collection<ModelMember>> selector,
            @Nonnull Function<ModelMember, Stream<ClassInfoModel>> dependencyExtractor) {
        return getMemberDependencies(selector,
                ModelUtils::defaultClassInfoMemberFilter, dependencyExtractor);
    }

    default <ModelMember extends Model> Collection<ClassInfoModel> getMemberDependencies(
            @Nonnull Function<ClassInfoModel, Collection<ModelMember>> selector,
            @Nonnull Predicate<ModelMember> filter,
            @Nonnull Function<ModelMember, Stream<ClassInfoModel>> dependencyExtractor) {
        return getMemberDependenciesStream(selector, filter,
                dependencyExtractor).collect(Collectors.toSet());
    }

    default <ModelMember extends Model> Stream<ClassInfoModel> getMemberDependenciesStream(
            @Nonnull Function<ClassInfoModel, Collection<ModelMember>> selector,
            @Nonnull Function<ModelMember, Stream<ClassInfoModel>> dependencyExtractor) {
        return getMemberDependenciesStream(selector,
                ModelUtils::defaultClassInfoMemberFilter, dependencyExtractor);
    }

    <ModelMember extends Model> Stream<ClassInfoModel> getMemberDependenciesStream(
            @Nonnull Function<ClassInfoModel, Collection<ModelMember>> selector,
            @Nonnull Predicate<ModelMember> filter,
            @Nonnull Function<ModelMember, Stream<ClassInfoModel>> dependencyExtractor);

    default <Member, DependableMember extends Model> Collection<DependableMember> getMembers(
            @Nonnull Function<ClassInfo, Collection<Member>> selector,
            @Nonnull BiFunction<Member, ClassInfoModel, DependableMember> wrapper) {
        return getMembers(selector, ModelUtils::defaultClassInfoMemberFilter,
                wrapper);
    }

    default <Member, ModelMember extends Model> Collection<ModelMember> getMembers(
            @Nonnull Function<ClassInfo, Collection<Member>> selector,
            @Nonnull Predicate<Member> filter,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(selector, filter, wrapper)
                .collect(Collectors.toList());
    }

    default <Member, ModelMember extends Model> Stream<ModelMember> getMembersStream(
            @Nonnull Function<ClassInfo, Collection<Member>> selector,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(selector,
                ModelUtils::defaultClassInfoMemberFilter, wrapper);
    }

    <Member, ModelMember extends Model> Stream<ModelMember> getMembersStream(
            @Nonnull Function<ClassInfo, Collection<Member>> selector,
            @Nonnull Predicate<Member> filter,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper);

    default Collection<ClassInfoModel> getMethodDependencies() {
        return getMethodDependenciesStream().collect(Collectors.toSet());
    }

    default Stream<ClassInfoModel> getMethodDependenciesStream() {
        return getMemberDependenciesStream(ClassInfoModel::getMethods,
                MethodInfoModel::getDependenciesStream);
    }

    Collection<MethodInfoModel> getMethods();

    default Stream<MethodInfoModel> getMethodsStream() {
        return getMethods().stream();
    }

    Optional<ClassInfoModel> getSuperClass();

    Collection<ClassInfoModel> getSuperClasses();

    default Stream<ClassInfoModel> getSuperClassesStream() {
        return getSuperClasses().stream();
    }
}
