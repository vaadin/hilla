package dev.hilla.parser.node;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class NodePath {
    private final LinkedList<Node<?, ?>> path = new LinkedList<>();

    NodePath(@Nonnull Node<?, ?> node, @Nonnull List<Node<?, ?>> ancestors) {
        this.path.add(Objects.requireNonNull(node));
        this.path.addAll(Objects.requireNonNull(ancestors));
    }

    @Nonnull
    public Node<?, ?> getNode() {
        return path.getFirst();
    }

    @Nonnull
    public RootNode getRootNode() {
        return (RootNode) path.getLast();
    }

    public boolean isRoot() {
        return path.size() == 1;
    }

    @Nonnull
    public NodePath getParentPath() {
        if (isRoot()) {
            return this;
        }

        var parentNode = path.get(1);
        if (path.size() == 2) {
            return new NodePath(parentNode, Collections.emptyList());
        }

        return new NodePath(parentNode, path.subList(2, path.size()));
    }

    @Override
    public int hashCode() {
        return path.hashCode() ^ 0xc859a12d;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NodePath nodePath = (NodePath) o;
        return path.equals(nodePath.path);
    }
}
