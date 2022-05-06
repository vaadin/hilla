package dev.hilla.parser.models;

import java.util.Objects;

import javax.annotation.Nonnull;

public abstract class AbstractModel<T> implements Model {
    protected final T origin;

    AbstractModel(@Nonnull T origin) {
        this.origin = Objects.requireNonNull(origin);
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
    public int hashCode() {
        return origin.hashCode();
    }

    @Override
    public String toString() {
        return origin.toString();
    }
}
