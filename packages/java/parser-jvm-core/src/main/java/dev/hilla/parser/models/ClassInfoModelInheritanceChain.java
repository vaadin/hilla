package dev.hilla.parser.models;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ClassInfo;

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
            @Nonnull Function<ClassInfo, Collection<Member>> selector,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(selector,
                ModelUtils::defaultClassInfoMemberFilter, wrapper)
                        .collect(Collectors.toSet());
    }

    public <Member, ModelMember extends Model> Collection<ModelMember> getMembers(
            @Nonnull Function<ClassInfo, Collection<Member>> selector,
            @Nonnull Predicate<Member> filter,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(selector, filter, wrapper)
                .collect(Collectors.toSet());
    }

    public <Member, ModelMember extends Model> Stream<ModelMember> getMembersStream(
            @Nonnull Function<ClassInfo, Collection<Member>> selector,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        return getMembersStream(selector,
                ModelUtils::defaultClassInfoMemberFilter, wrapper);
    }

    public <Member, ModelMember extends Model> Stream<ModelMember> getMembersStream(
            @Nonnull Function<ClassInfo, Collection<Member>> selector,
            @Nonnull Predicate<Member> filter,
            @Nonnull BiFunction<Member, ClassInfoModel, ModelMember> wrapper) {
        Objects.requireNonNull(wrapper);
        Objects.requireNonNull(filter);
        Objects.requireNonNull(selector);

        return getClasses().stream()
                .flatMap(cls -> cls.getMembersStream(selector, filter, wrapper))
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
