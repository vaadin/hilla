package com.vaadin.fusion.parser.core;

import java.util.stream.Stream;

import io.github.classgraph.FieldInfo;

public class RelativeFieldInfo
        extends AbstractRelative<FieldInfo, RelativeClassInfo> {
    private final RelativeTypeSignature type;

    public RelativeFieldInfo(FieldInfo origin, RelativeClassInfo parent) {
        super(origin, parent);
        type = RelativeTypeSignature
                .of(origin.getTypeSignatureOrTypeDescriptor(), this);
    }

    @Override
    public Stream<RelativeClassInfo> getDependencies() {
        return type.getDependencies();
    }

    public RelativeTypeSignature getType() {
        return type;
    }
}
