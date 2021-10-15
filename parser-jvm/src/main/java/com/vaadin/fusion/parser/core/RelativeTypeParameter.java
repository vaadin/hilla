package com.vaadin.fusion.parser.core;

import java.util.List;
import java.util.stream.Collectors;

import io.github.classgraph.TypeParameter;

public class RelativeTypeParameter
        extends AbstractRelative<TypeParameter, Relative<?>> {
    private final RelativeTypeSignature classBound;
    private final List<RelativeTypeSignature> interfaceBounds;

    public RelativeTypeParameter(TypeParameter origin, Relative<?> parent) {
        super(origin, parent);
        classBound = RelativeTypeSignature.of(origin.getClassBound(), this);
        interfaceBounds = origin.getInterfaceBounds().stream()
                .map(signature -> RelativeTypeSignature.of(signature, this))
                .collect(Collectors.toList());
    }

    public RelativeTypeSignature getClassBound() {
        return classBound;
    }

    public List<RelativeTypeSignature> getInterfaceBounds() {
        return interfaceBounds;
    }
}
