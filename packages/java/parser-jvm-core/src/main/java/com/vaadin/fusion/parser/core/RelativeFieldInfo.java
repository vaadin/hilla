package com.vaadin.fusion.parser.core;

import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.FieldInfo;

public final class RelativeFieldInfo
        extends AbstractRelative<FieldInfo, RelativeClassInfo> {
    private final RelativeTypeSignature type;

    public RelativeFieldInfo(@Nonnull FieldInfo origin,
            @Nonnull RelativeClassInfo parent) {
        super(origin, Objects.requireNonNull(parent));
        type = RelativeTypeSignature
                .of(origin.getTypeSignatureOrTypeDescriptor(), this);
    }

    @Override
    public Stream<RelativeClassInfo> getDependenciesStream() {
        return type.getDependenciesStream();
    }

    public RelativeTypeSignature getType() {
        return type;
    }
}
