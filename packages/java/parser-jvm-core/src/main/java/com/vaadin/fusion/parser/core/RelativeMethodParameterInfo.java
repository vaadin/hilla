package com.vaadin.fusion.parser.core;

import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.MethodParameterInfo;

public final class RelativeMethodParameterInfo
        extends AbstractRelative<MethodParameterInfo, RelativeMethodInfo> {
    private final RelativeTypeSignature type;

    public RelativeMethodParameterInfo(@Nonnull MethodParameterInfo origin,
            @Nonnull RelativeMethodInfo parent) {
        super(origin, Objects.requireNonNull(parent));

        type = RelativeTypeSignature
                .of(origin.getTypeSignatureOrTypeDescriptor(), this);
    }

    @Override
    public Stream<RelativeClassInfo> getDependenciesStream() {
        return getType().getDependenciesStream();
    }

    public RelativeTypeSignature getType() {
        return type;
    }
}
