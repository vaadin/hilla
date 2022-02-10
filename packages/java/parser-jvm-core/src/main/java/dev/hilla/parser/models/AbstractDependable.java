package dev.hilla.parser.models;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

abstract class AbstractDependable<T, P extends Dependable<?, ?>>
        implements Dependable<T, P> {
    protected final T origin;
    protected final P parent;

    public AbstractDependable(@Nonnull T origin, P parent) {
        this.origin = Objects.requireNonNull(origin);
        this.parent = parent;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof AbstractDependable<?, ?>)) {
            return false;
        }

        return Objects.equals(origin,
                ((AbstractDependable<?, ?>) other).origin);
    }

    @Override
    public T get() {
        return origin;
    }

    @Override
    public Optional<P> getParent() {
        return Optional.of(parent);
    }

    @Override
    public int hashCode() {
        return origin.hashCode();
    }

    @Override
    public String toString() {
        return origin.toString();
    }
}
