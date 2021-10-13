package com.vaadin.fusion.parser.core;

import java.util.stream.Stream;

import io.github.classgraph.MethodParameterInfo;

public class RelativeMethodParameterInfo implements Relative, RelativeMember {
    private final MethodParameterInfo origin;

    public RelativeMethodParameterInfo(MethodParameterInfo origin) {
        this.origin = origin;
    }

    @Override
    public MethodParameterInfo get() {
        return origin;
    }

    public Stream<RelativeClassInfo> getDependencies() {
        return getType().getDependencies();
    }

    @Override
    public RelativeClassInfo getHost() {
        return new RelativeClassInfo(origin.getMethodInfo().getClassInfo());
    }

    public RelativeTypeSignature getType() {
        return RelativeTypeSignature
                .of(origin.getTypeSignatureOrTypeDescriptor());
    }
}
