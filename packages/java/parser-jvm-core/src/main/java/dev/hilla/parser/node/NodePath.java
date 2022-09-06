package dev.hilla.parser.node;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class NodePath<N extends Node<?, ?>> {
    private final N node;
    private final NodePath<?> parentPath;
    private final NodePath<RootNode> rootPath;

    public NodePath(@Nonnull N node, @Nonnull NodePath<?> parentPath) {
        this.node = Objects.requireNonNull(node);
        this.parentPath = Objects.requireNonNull(parentPath);
        this.rootPath = Objects.requireNonNull(parentPath.getRootPath());
    }

    @SuppressWarnings("unchecked")
    NodePath(@Nonnull N node) {
        this.node = Objects.requireNonNull(node);
        this.parentPath = this;
        this.rootPath = (NodePath<RootNode>) this;
    }

    public Node<?, ?> getNode() {
        return node;
    }

    public NodePath<?> getParentPath() {
        return parentPath;
    }

    public NodePath<RootNode> getRootPath() {
        return rootPath;
    }

    private boolean hasParentNodes() {
        return getNode() != getParentPath().getNode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        var otherPath = (NodePath<?>) o;
        var thisPath = (NodePath<?>) this;
        while (thisPath.getNode().equals(otherPath.getNode())) {
            if (thisPath.hasParentNodes() || otherPath.hasParentNodes()) {
                thisPath = thisPath.getParentPath();
                otherPath = otherPath.getParentPath();
            } else {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0xa73fc160 ^ node.hashCode();
        var thisPath = (NodePath<?>) this;
        while (thisPath.hasParentNodes()) {
            thisPath = thisPath.getParentPath();
            hash ^= thisPath.getNode().hashCode();
        }
        return hash;
    }
}
