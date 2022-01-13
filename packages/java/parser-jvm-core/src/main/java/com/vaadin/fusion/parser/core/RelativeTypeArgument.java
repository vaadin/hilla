package com.vaadin.fusion.parser.core;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.TypeArgument;

public final class RelativeTypeArgument
        extends AbstractRelative<TypeArgument, Relative<?>>
        implements RelativeTypeSignature {
    private final RelativeTypeSignature wildcardAssociatedType;

    private RelativeTypeArgument(TypeArgument origin, Relative<?> parent) {
        super(origin, parent);
        wildcardAssociatedType = RelativeTypeSignature
                .ofNullable(origin.getTypeSignature(), this);
    }

    static RelativeTypeArgument of(@Nonnull TypeArgument origin,
            @Nonnull Relative<?> parent) {
        return Pool.createInstance(origin, Objects.requireNonNull(parent),
                RelativeTypeArgument::new);
    }

    public static Stream<ClassInfo> resolve(TypeArgument signature) {
        return RelativeTypeSignature.resolve(signature.getTypeSignature());
    }

    @Override
    public TypeArgument get() {
        return origin;
    }

    public TypeArgument.Wildcard getWildcard() {
        return origin.getWildcard();
    }

    public Optional<RelativeTypeSignature> getWildcardAssociatedType() {
        return Optional.ofNullable(wildcardAssociatedType);
    }

    @Override
    public boolean isTypeArgument() {
        return true;
    }
}
