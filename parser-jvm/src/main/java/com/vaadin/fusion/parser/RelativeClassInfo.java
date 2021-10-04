package com.vaadin.fusion.parser;

import java.util.stream.Stream;

import io.github.classgraph.ClassInfo;

public class RelativeClassInfo implements Relative {
    private final ClassInfo classInfo;

    RelativeClassInfo(ClassInfo classInfo) {
        this.classInfo = classInfo;
    }

    @Override
    public ClassInfo get() {
        return classInfo;
    }

    public RelativeClassStream getDependencies() {
        return RelativeClassStream.of(getFieldDependencies().stream(),
                getInnerClassDependencies().stream(),
                getMethodDependencies().stream(),
                getSuperDependencies().stream());
    }

    public RelativeClassStream getFieldDependencies() {
        return RelativeClassStream.of(getInheritanceChain().getFields()
                .flatMap(field -> field.getDependencies().stream()));
    }

    public RelativeClassStream getInnerClassDependencies() {
        return getInheritanceChain().getInnerClasses();
    }

    public RelativeClassStream getMethodDependencies() {
        return RelativeClassStream.of(getInheritanceChain().getMethods()
                .flatMap(method -> method.getDependencies().stream()));
    }

    public RelativeClassStream getSuperDependencies() {
        return RelativeClassStream.ofRaw(classInfo.getSuperclasses().stream());
    }

    public RelativeClassStream getInheritanceChain() {
        return RelativeClassStream.ofRaw(Stream.of(classInfo),
                classInfo.getSuperclasses().stream());
    }
}
