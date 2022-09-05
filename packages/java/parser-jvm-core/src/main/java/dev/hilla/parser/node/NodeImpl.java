package dev.hilla.parser.node;

import javax.annotation.Nonnull;
import java.util.Objects;

import io.swagger.v3.oas.models.media.Schema;

public class NodeImpl<S, T> implements Node<S, T> {
    private S source;

    private T target;

    public NodeImpl(@Nonnull S value, @Nonnull T schema) {
        this.source = Objects.requireNonNull(value);
        this.target = Objects.requireNonNull(schema);
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NodeImpl<?, ?> node = (NodeImpl<?, ?>) o;
        return source.equals(node.getSource()) && target.equals(node.getTarget());
    }

    @Override
    public int hashCode() {
        return source.hashCode() ^ target.hashCode() ^ 0x042ebeb0;
    }
}
