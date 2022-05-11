package dev.hilla.parser.models;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

public abstract class AbstractModel<T> implements Model {
    protected final T origin;
    protected final Model parent;

    AbstractModel(@Nonnull T origin, Model parent) {
        this.origin = Objects.requireNonNull(origin);
        this.parent = parent;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof AbstractModel)) {
            return false;
        }

        return Objects.equals(origin, ((AbstractModel<?>) other).origin);
    }

    @Override
    public T get() {
        return origin;
    }

    @Override
    public Optional<Model> getParent() {
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
