package dev.hilla.parser.core;

import java.util.Objects;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.NamedModel;
import dev.hilla.parser.models.SignatureModel;

public abstract class AbstractNode<S, T> implements Node<S, T> {
    private final S source;

    private T target;

    protected AbstractNode(@Nonnull S source, T target) {
        this.source = Objects.requireNonNull(source);
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var node = (AbstractNode<?, ?>) o;
        return source.equals(node.getSource());
    }

    @Override
    public S getSource() {
        return source;
    }

    @Override
    public T getTarget() {
        return target;
    }

    @Override
    public void setTarget(T target) {
        this.target = target;
    }

    @Override
    public int hashCode() {
        return source.hashCode() ^ 0x042ebeb0;
    }

    @Override
    public String toString() {
        var sourceName = "";
        if (source instanceof ClassInfoModel) {
            sourceName = ((ClassInfoModel) source).getSimpleName();
        } else if (source instanceof SignatureModel) {
            sourceName = source.toString();
        } else if (source instanceof NamedModel) {
            sourceName = ((NamedModel) source).getName();
        } else {
            sourceName = source.getClass().getSimpleName();
        }

        return getClass().getSimpleName().replaceAll("Node$", "") + "("
                + sourceName + ")";
    }
}
