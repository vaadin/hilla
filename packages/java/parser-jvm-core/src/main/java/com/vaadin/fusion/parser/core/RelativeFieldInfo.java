package com.vaadin.fusion.parser.core;

import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.FieldInfo;

public final class RelativeFieldInfo
        extends AbstractRelative<FieldInfo, RelativeClassInfo> {
    private final RelativeTypeSignature type;

    private RelativeFieldInfo(FieldInfo origin, RelativeClassInfo parent) {
        super(origin, parent);
        type = RelativeTypeSignature
                .of(origin.getTypeSignatureOrTypeDescriptor(), this);
    }

    public static RelativeFieldInfo of(@Nonnull FieldInfo origin,
            @Nonnull RelativeClassInfo parent) {
        return Pool.createInstance(origin, Objects.requireNonNull(parent),
                RelativeFieldInfo::new);
    }

    @Override
    public Stream<RelativeClassInfo> getDependenciesStream() {
        return type.getDependenciesStream();
    }

    public RelativeTypeSignature getType() {
        return type;
    }
}
