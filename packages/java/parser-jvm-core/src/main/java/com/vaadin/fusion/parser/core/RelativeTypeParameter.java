package com.vaadin.fusion.parser.core;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
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
        var classBound = origin.getClassBound();
        this.classBound = classBound != null
                ? RelativeTypeSignature.of(classBound, this)
                : null;
        interfaceBounds = origin.getInterfaceBounds().stream()
                .map(signature -> RelativeTypeSignature.of(signature, this))
                .collect(Collectors.toList());
    }

    public Stream<RelativeTypeSignature> getAllBoundsStream() {
        return Stream.of(Stream.of(classBound), interfaceBounds.stream())
                .flatMap(Function.identity());
    }

    public Optional<RelativeTypeSignature> getClassBound() {
        return Optional.ofNullable(classBound);
    }

    public List<RelativeTypeSignature> getInterfaceBounds() {
        return interfaceBounds;
    }

    public Stream<RelativeTypeSignature> getInterfaceBoundsStream() {
        return interfaceBounds.stream();
    }

    @Override
    public boolean isTypeParameter() {
        return true;
    }
}
