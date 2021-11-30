package com.vaadin.fusion.parser.core;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.TypeParameter;

public final class RelativeTypeParameter
        extends AbstractRelative<TypeParameter, Relative<?>>
        implements RelativeTypeSignature {
    private final RelativeTypeSignature classBound;
    private final List<RelativeTypeSignature> interfaceBounds;

    public RelativeTypeParameter(@Nonnull TypeParameter origin,
            @Nonnull Relative<?> parent) {
        super(origin, Objects.requireNonNull(parent));
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

    public Stream<RelativeTypeSignature> getInterfaceBoundsStream() {
        return interfaceBounds.stream();
    }
}
