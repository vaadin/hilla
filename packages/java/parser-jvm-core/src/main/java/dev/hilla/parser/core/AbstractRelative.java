package dev.hilla.parser.core;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

abstract class AbstractRelative<T, P extends Relative<?>>
        implements Relative<P> {
    protected final T origin;
    protected final P parent;

    public AbstractRelative(@Nonnull T origin, P parent) {
        this.origin = Objects.requireNonNull(origin);
        this.parent = parent;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof AbstractRelative<?, ?>)) {
            return false;
        }

        return Objects.equals(origin, ((AbstractRelative<?, ?>) other).origin);
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
