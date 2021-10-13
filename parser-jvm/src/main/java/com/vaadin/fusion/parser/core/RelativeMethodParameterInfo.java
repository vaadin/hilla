package com.vaadin.fusion.parser.core;

import java.util.Optional;
import java.util.stream.Stream;

import io.github.classgraph.MethodParameterInfo;

public class RelativeMethodParameterInfo
        extends AbstractRelative<MethodParameterInfo, RelativeMethodInfo> {
    private final RelativeTypeSignature type;

    public RelativeMethodParameterInfo(MethodParameterInfo origin,
            RelativeMethodInfo parent) {
        super(origin, parent);

        type = RelativeTypeSignature
                .of(origin.getTypeSignatureOrTypeDescriptor(), this);
    }

    @Override
    public Stream<RelativeClassInfo> getDependencies() {
        return getType().getDependencies();
    }

    public RelativeTypeSignature getType() {
        return type;
    }
}
