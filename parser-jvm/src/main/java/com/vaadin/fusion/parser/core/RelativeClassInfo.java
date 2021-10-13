package com.vaadin.fusion.parser.core;

import java.util.function.Function;
import java.util.stream.Stream;

import io.github.classgraph.ClassInfo;

public class RelativeClassInfo implements Relative {
    private final ClassInfo origin;

    public RelativeClassInfo(ClassInfo origin) {
        this.origin = origin;
    }

    @Override
    public ClassInfo get() {
        return origin;
    }

    public Stream<RelativeAnnotationInfo> getAnnotations() {
        return origin.getAnnotationInfo().stream()
                .map(RelativeAnnotationInfo::new);
    }

    public Stream<RelativeClassInfo> getDependencies() {
        return Stream
                .of(getFieldDependencies(), getMethodDependencies(),
                        getInnerClassDependencies(), getSuperClasses())
                .flatMap(Function.identity());
    }

    public Stream<RelativeClassInfo> getFieldDependencies() {
        return getFields().flatMap(RelativeFieldInfo::getDependencies);
    }

    public Stream<RelativeFieldInfo> getFields() {
        return origin.getFieldInfo().stream().map(RelativeFieldInfo::new);
    }

    public Stream<RelativeClassInfo> getInheritanceChain() {
        return Stream
                .of(Stream.of(this),
                        origin.getSuperclasses().stream()
                                .map(RelativeClassInfo::new))
                .flatMap(Function.identity());
    }

    public Stream<RelativeClassInfo> getInnerClassDependencies() {
        return getInheritanceChain()
                .flatMap(RelativeClassInfo::getInnerClasses);
    }

    public Stream<RelativeClassInfo> getInnerClasses() {
        return origin.getInnerClasses().stream().map(RelativeClassInfo::new);
    }

    public Stream<RelativeClassInfo> getMethodDependencies() {
        return getInheritanceChain().flatMap(RelativeClassInfo::getMethods)
                .flatMap(RelativeMethodInfo::getDependencies);
    }

    public Stream<RelativeMethodInfo> getMethods() {
        return origin.getMethodInfo().stream().map(RelativeMethodInfo::new);
    }

    public Stream<RelativeClassInfo> getSuperClasses() {
        return origin.getSuperclasses().stream().map(RelativeClassInfo::new);
    }
}
