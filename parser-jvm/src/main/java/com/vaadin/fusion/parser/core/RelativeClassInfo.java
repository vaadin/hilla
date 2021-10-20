package com.vaadin.fusion.parser.core;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ClassInfo;

public class RelativeClassInfo
        extends AbstractRelative<ClassInfo, RelativeClassInfo> {
    private final List<RelativeAnnotationInfo> annotations;
    private final List<RelativeFieldInfo> fields;
    private final List<RelativeClassInfo> innerClasses;
    private final List<RelativeMethodInfo> methods;
    private final List<RelativeClassInfo> superClasses;

    public RelativeClassInfo(@Nonnull ClassInfo origin) {
        this(origin, null);
    }

    public RelativeClassInfo(@Nonnull ClassInfo origin, RelativeClassInfo parent) {
        super(origin, parent);

        annotations = getMembers(ClassInfo::getAnnotationInfo,
                RelativeAnnotationInfo::new);
        fields = getMembers(ClassInfo::getFieldInfo,
                RelativeFieldInfo::new);
        methods = getMembers(ClassInfo::getMethodInfo,
                RelativeMethodInfo::new);
        superClasses = getMembers(ClassInfo::getSuperclasses,
                RelativeClassInfo::new);
        innerClasses = getMembers(ClassInfo::getInnerClasses,
                RelativeClassInfo::new);
    }

    public List<RelativeAnnotationInfo> getAnnotations() {
        return annotations;
    }

    @Override
    public Stream<RelativeClassInfo> getDependencies() {
        return Stream
                .of(getFieldDependencies(), getMethodDependencies(),
                        innerClasses.stream(), getInnerClassDependencies(),
                        superClasses.stream(), getSuperClassesDependencies())
                .flatMap(Function.identity()).distinct();
    }

    public Stream<RelativeClassInfo> getFieldDependencies() {
        return getMemberDependencies(RelativeClassInfo::getFields,
                RelativeFieldInfo::getDependencies);
    }

    public List<RelativeFieldInfo> getFields() {
        return fields;
    }

    public Stream<RelativeClassInfo> getInheritanceChain() {
        return Stream.of(Stream.of(this), getSuperClasses().stream())
                .flatMap(Function.identity()).distinct();
    }

    public Stream<RelativeClassInfo> getInnerClassDependencies() {
        return getMemberDependencies(RelativeClassInfo::getInnerClasses,
                RelativeClassInfo::getDependencies);
    }

    public List<RelativeClassInfo> getInnerClasses() {
        return innerClasses;
    }

    public <RelativeMember> Stream<RelativeClassInfo> getMemberDependencies(
            @Nonnull Function<RelativeClassInfo, List<RelativeMember>> selector,
            @Nonnull Function<RelativeMember, Stream<RelativeClassInfo>> dependencyExtractor) {
        Objects.requireNonNull(selector);
        return getInheritanceChain()
                .flatMap(cls -> selector.apply(cls).stream())
                .flatMap(Objects.requireNonNull(dependencyExtractor)).distinct();
    }

    public <Member, RelativeMember> List<RelativeMember> getMembers(
            @Nonnull Function<ClassInfo, List<Member>> selector,
            @Nonnull BiFunction<Member, RelativeClassInfo, RelativeMember> wrapper) {
        Objects.requireNonNull(wrapper);
        return Objects.requireNonNull(selector).apply(origin).stream()
                .map(member -> wrapper.apply(member, this))
                .collect(Collectors.toList());
    }

    public Stream<RelativeClassInfo> getMethodDependencies() {
        return getMemberDependencies(RelativeClassInfo::getMethods,
                RelativeMethodInfo::getDependencies);
    }

    public List<RelativeMethodInfo> getMethods() {
        return methods;
    }

    @Override
    public Optional<RelativeClassInfo> getParent() {
        return Optional.ofNullable(parent);
    }

    public List<RelativeClassInfo> getSuperClasses() {
        return superClasses;
    }

    public Stream<RelativeClassInfo> getSuperClassesDependencies() {
        return getMemberDependencies(RelativeClassInfo::getSuperClasses,
                RelativeClassInfo::getDependencies);
    }
}
