package dev.hilla.parser.core;

import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.FieldInfo;

public final class RelativeFieldInfo
        extends AbstractRelative<FieldInfo, RelativeClassInfo> {
    private RelativeTypeSignature type;

    public RelativeFieldInfo(@Nonnull FieldInfo origin,
            @Nonnull RelativeClassInfo parent) {
        super(origin, Objects.requireNonNull(parent));
    }

    @Override
    public Stream<RelativeClassInfo> getDependenciesStream() {
        return getType().getDependenciesStream();
    }

    public RelativeTypeSignature getType() {
        if (type == null) {
            type = RelativeTypeSignature
                    .of(origin.getTypeSignatureOrTypeDescriptor(), this);
        }

        return type;
    }
}
