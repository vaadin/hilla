package com.vaadin.fusion.parser.core;

import static com.vaadin.fusion.parser.core.EnhancedTypeSignature.resolve;

import java.util.Arrays;

import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodParameterInfo;

public class RelativeMethodInfo implements Relative, RelativeMember {
    private final MethodInfo methodInfo;
    private final RelativeClassInfo host;

    RelativeMethodInfo(final MethodInfo methodInfo) {
        this.methodInfo = methodInfo;
        host = new RelativeClassInfo(methodInfo.getClassInfo());
    }

    @Override
    public MethodInfo get() {
        return methodInfo;
    }

    @Override
    public RelativeClassInfo getHost() {
        return host;
    }

    public RelativeClassStream getDependencies() {
        return RelativeClassStream.of(getResultDependencies().stream(),
                getParameterDependencies().stream());
    }

    public RelativeClassStream getParameterDependencies() {
        return RelativeClassStream
                .ofRaw(resolve(Arrays.stream(methodInfo.getParameterInfo())
                        .map(MethodParameterInfo::getTypeSignature)));
    }

    public RelativeClassStream getResultDependencies() {
        return RelativeClassStream.ofRaw(resolve(
                methodInfo.getTypeSignatureOrTypeDescriptor().getResultType()));
    }
}
