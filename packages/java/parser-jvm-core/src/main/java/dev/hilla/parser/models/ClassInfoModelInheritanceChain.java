package dev.hilla.parser.models;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class ClassInfoModelInheritanceChain {
    private final ClassInfoModel model;
    private Collection<ClassInfoModel> chain;

    ClassInfoModelInheritanceChain(ClassInfoModel model) {
        this.model = model;
    }

    public Collection<ClassInfoModel> getClasses() {
        if (chain == null) {
            var superClasses = model.getSuperClasses();
            chain = new LinkedHashSet<>(superClasses.size() + 1);
            chain.add(model);
            chain.addAll(superClasses);
        }

        return chain;
    }

    public Stream<ClassInfoModel> getClassesStream() {
        return getClasses().stream();
    }

    public Collection<ClassInfoModel> getDependencies() {
        return getDependenciesStream().collect(Collectors.toSet());
    }

    public Stream<ClassInfoModel> getDependenciesStream() {
        return getClasses().stream()
                .flatMap(ClassInfoModel::getDependenciesStream).distinct();
    }

    public Collection<ClassInfoModel> getFieldDependencies() {
        return getFieldDependenciesStream().collect(Collectors.toSet());
    }

    public Stream<ClassInfoModel> getFieldDependenciesStream() {
        return getClasses().stream()
                .flatMap(ClassInfoModel::getFieldDependenciesStream).distinct();
    }

    public Collection<FieldInfoModel> getFields() {
        return getFieldsStream().collect(Collectors.toSet());
    }

    public Stream<FieldInfoModel> getFieldsStream() {
        return getClasses().stream().flatMap(ClassInfoModel::getFieldsStream)
                .distinct();
    }

    public Collection<ClassInfoModel> getInnerClassDependencies() {
        return getInnerClassDependenciesStream().collect(Collectors.toSet());
    }

    public Stream<ClassInfoModel> getInnerClassDependenciesStream() {
        return getClasses().stream()
                .flatMap(ClassInfoModel::getInnerClassDependenciesStream)
                .distinct();
    }

    public Collection<ClassInfoModel> getInnerClasses() {
        return getInnerClassesStream().collect(Collectors.toSet());
    }

    public Stream<ClassInfoModel> getInnerClassesStream() {
        return getClasses().stream()
                .flatMap(ClassInfoModel::getInnerClassesStream).distinct();
    }

    public <Member, ModelMember extends Model> Collection<ModelMember> getMembers(
            @Nonnull Collection<Member> members,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(members,
                ClassInfoModelUtils::defaultClassInfoMemberFilter, wrapper)
                        .collect(Collectors.toSet());
    }

    public <Member, ModelMember extends Model> Collection<ModelMember> getMembers(
            @Nonnull Collection<Member> members,
            @Nonnull Predicate<Member> filter,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(members.stream(), filter, wrapper)
                .collect(Collectors.toSet());
    }

    public <Member, ModelMember extends Model> Collection<ModelMember> getMembers(
            @Nonnull Stream<Member> members,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(members,
                ClassInfoModelUtils::defaultClassInfoMemberFilter, wrapper)
                        .collect(Collectors.toSet());
    }

    public <Member, ModelMember extends Model> Collection<ModelMember> getMembers(
            @Nonnull Stream<Member> members, @Nonnull Predicate<Member> filter,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(members, filter, wrapper)
                .collect(Collectors.toSet());
    }

    public <Member, ModelMember extends Model> Stream<ModelMember> getMembersStream(
            @Nonnull Collection<Member> members,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(members,
                ClassInfoModelUtils::defaultClassInfoMemberFilter, wrapper);
    }

    public <Member, ModelMember extends Model> Stream<ModelMember> getMembersStream(
            @Nonnull Collection<Member> members,
            @Nonnull Predicate<Member> filter,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(Objects.requireNonNull(members).stream(),
                filter, wrapper);
    }

    public <Member, ModelMember extends Model> Stream<ModelMember> getMembersStream(
            @Nonnull Stream<Member> members,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(members,
                ClassInfoModelUtils::defaultClassInfoMemberFilter, wrapper);
    }

    public <Member, ModelMember extends Model> Stream<ModelMember> getMembersStream(
            @Nonnull Stream<Member> members, @Nonnull Predicate<Member> filter,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        Objects.requireNonNull(wrapper);
        Objects.requireNonNull(filter);
        Objects.requireNonNull(members);

        return getClasses().stream()
                .flatMap(cls -> cls.getMembersStream(members, filter, wrapper))
                .distinct();
    }

    public Collection<ClassInfoModel> getMethodDependencies() {
        return getMethodDependenciesStream().collect(Collectors.toSet());
    }

    public Stream<ClassInfoModel> getMethodDependenciesStream() {
        return getClasses().stream()
                .flatMap(ClassInfoModel::getMethodDependenciesStream)
                .distinct();
    }

    public Collection<MethodInfoModel> getMethods() {
        return getMethodsStream().collect(Collectors.toSet());
    }

    public Stream<MethodInfoModel> getMethodsStream() {
        return getClasses().stream().flatMap(ClassInfoModel::getMethodsStream)
                .distinct();
    }
}
