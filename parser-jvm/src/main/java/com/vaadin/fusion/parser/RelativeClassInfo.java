package com.vaadin.fusion.parser;

import java.util.stream.Stream;

import io.github.classgraph.ClassInfo;

public class RelativeClassInfo implements Relative {
    private final ClassInfo classInfo;
    private final StreamAPI streamAPI = new StreamAPI();

    RelativeClassInfo(ClassInfo classInfo) {
        this.classInfo = classInfo;
    }

    @Override
    public StreamAPI asStream() {
        return streamAPI;
    }

    @Override
    public ClassInfo get() {
        return classInfo;
    }

    public RelativeClassList getFieldDependencies() {
        return streamAPI.getFieldDependencies().collectToList();
    }

    public RelativeClassList getInnerClassDependencies() {
        return streamAPI.getInnerClassDependencies().collectToList();
    }

    public RelativeClassList getMethodDependencies() {
        return streamAPI.getMethodDependencies().collectToList();
    }

    public RelativeClassList getSuperDependencies() {
        return streamAPI.getSuperDependencies().collectToList();
    }

    public final class StreamAPI {
        public RelativeClassStream getFieldDependencies() {
            return new RelativeClassStream(
                    getInheritanceChain().getFields().flatMap(field -> field
                            .asStream().getDependencies().unwrap()));
        }

        public RelativeClassStream getInnerClassDependencies() {
            return getInheritanceChain().getInnerClasses();
        }

        public RelativeClassStream getMethodDependencies() {
            return new RelativeClassStream(
                    getInheritanceChain().getMethods().flatMap(method -> method
                            .asStream().getDependencies().unwrap()));
        }

        public RelativeClassStream getSuperDependencies() {
            return new RelativeClassStream(classInfo.getSuperclasses().stream()
                    .map(RelativeClassInfo::new));
        }

        public RelativeClassStream getInheritanceChain() {
            return new RelativeClassStream(Stream
                    .concat(Stream.of(classInfo),
                            classInfo.getSuperclasses().stream())
                    .map(RelativeClassInfo::new));
        }
    }
}
