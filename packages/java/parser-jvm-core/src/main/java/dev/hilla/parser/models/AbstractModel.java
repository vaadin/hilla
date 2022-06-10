package dev.hilla.parser.models;

import java.util.Objects;

import javax.annotation.Nonnull;

public abstract class AbstractModel<T> implements Model {
    protected final T origin;

    AbstractModel(@Nonnull T origin) {
        this.origin = Objects.requireNonNull(origin);
    }

    @Override
    public T get() {
        return origin;
    }
}
