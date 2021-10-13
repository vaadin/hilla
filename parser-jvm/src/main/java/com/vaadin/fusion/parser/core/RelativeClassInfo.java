package com.vaadin.fusion.parser.core;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.ClassInfo;

public class RelativeClassInfo
        extends AbstractRelative<ClassInfo, RelativeClassInfo> {
    private final List<RelativeAnnotationInfo> annotations;
    private final List<RelativeFieldInfo> fields;
    private final List<RelativeMethodInfo> methods;
    private final List<RelativeClassInfo> superClasses;
    private final List<RelativeClassInfo> innerClasses;

    public RelativeClassInfo(ClassInfo origin) {
        this(origin, null);
    }

    public RelativeClassInfo(ClassInfo origin, RelativeClassInfo parent) {
        super(origin, parent);

        annotations = origin.getAnnotationInfo().stream()
                .map(value -> new RelativeAnnotationInfo(value, this))
                .collect(Collectors.toList());
        fields = origin.getFieldInfo().stream()
                .map(value -> new RelativeFieldInfo(value, this))
                .collect(Collectors.toList());
        methods = origin.getMethodInfo().stream()
                .map(value -> new RelativeMethodInfo(value, this))
                .collect(Collectors.toList());
        superClasses = origin.getSuperclasses().stream()
                .map(RelativeClassInfo::new).collect(Collectors.toList());
        innerClasses = origin.getInnerClasses().stream()
                .map(RelativeClassInfo::new).collect(Collectors.toList());
    }

    @Override
    public Optional<RelativeClassInfo> getParent() {
        return Optional.ofNullable(parent);
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
        return getFields().stream().flatMap(RelativeFieldInfo::getDependencies)
                .distinct();
    }

    public List<RelativeFieldInfo> getFields() {
        return fields;
    }

    public Stream<RelativeClassInfo> getInheritanceChain() {
        return Stream.of(Stream.of(this), getSuperClasses().stream())
                .flatMap(Function.identity()).distinct();
    }

    public Stream<RelativeClassInfo> getInnerClassDependencies() {
        return getInheritanceChain()
                .flatMap(cls -> cls.getInnerClasses().stream())
                .flatMap(RelativeClassInfo::getDependencies).distinct();
    }

    public List<RelativeClassInfo> getInnerClasses() {
        return innerClasses;
    }

    public Stream<RelativeClassInfo> getMethodDependencies() {
        return getInheritanceChain().flatMap(cls -> cls.getMethods().stream())
                .flatMap(RelativeMethodInfo::getDependencies).distinct();
    }

    public List<RelativeMethodInfo> getMethods() {
        return methods;
    }

    public Stream<RelativeClassInfo> getSuperClassesDependencies() {
        return superClasses.stream().flatMap(RelativeClassInfo::getDependencies)
                .distinct();
    }

    public List<RelativeClassInfo> getSuperClasses() {
        return superClasses;
    }
}
