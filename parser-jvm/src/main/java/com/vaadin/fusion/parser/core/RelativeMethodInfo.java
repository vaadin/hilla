package com.vaadin.fusion.parser.core;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import io.github.classgraph.MethodInfo;

public class RelativeMethodInfo implements Relative, RelativeMember {
    private final MethodInfo methodInfo;

    public RelativeMethodInfo(MethodInfo methodInfo) {
        this.methodInfo = methodInfo;
    }

    @Override
    public MethodInfo get() {
        return methodInfo;
    }

    public Stream<RelativeClassInfo> getDependencies() {
        return Stream.of(getResultDependencies(), getParameterDependencies())
                .flatMap(Function.identity());
    }

    @Override
    public RelativeClassInfo getHost() {
        return new RelativeClassInfo(methodInfo.getClassInfo());
    }

    public Stream<RelativeClassInfo> getParameterDependencies() {
        return getParameters()
                .flatMap(RelativeMethodParameterInfo::getDependencies);
    }

    public Stream<RelativeMethodParameterInfo> getParameters() {
        return Arrays.stream(methodInfo.getParameterInfo())
                .map(RelativeMethodParameterInfo::new);
    }

    public Stream<RelativeClassInfo> getResultDependencies() {
        return getResultType().getDependencies();
    }

    public RelativeTypeSignature getResultType() {
        return RelativeTypeSignature.of(
                methodInfo.getTypeSignatureOrTypeDescriptor().getResultType());
    }
}
