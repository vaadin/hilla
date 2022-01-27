package dev.hilla.parser.core;

import java.util.Optional;
import java.util.stream.Stream;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.TypeArgument;

public final class RelativeTypeArgument
        extends AbstractRelative<TypeArgument, Relative<?>>
        implements RelativeTypeSignature {
    private final RelativeTypeSignature wildcardAssociatedType;

    RelativeTypeArgument(TypeArgument origin, Relative<?> parent) {
        super(origin, parent);
        wildcardAssociatedType = RelativeTypeSignature
                .ofNullable(origin.getTypeSignature(), this);
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
