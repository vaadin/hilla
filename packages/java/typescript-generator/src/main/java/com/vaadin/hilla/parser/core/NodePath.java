package com.vaadin.hilla.parser.core;

import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

public final class NodePath<N extends Node<?, ?>> {
    private final N node;
    private final NodePath<?> parentPath;
    private final NodePath<RootNode> rootPath;

    private NodePath(@NonNull N node, @NonNull NodePath<?> parentPath) {
        this.node = Objects.requireNonNull(node);
        this.parentPath = Objects.requireNonNull(parentPath);
        this.rootPath = Objects.requireNonNull(parentPath.getRootPath());
    }

    @SuppressWarnings("unchecked")
    private NodePath(@NonNull N node) {
        if (!(node instanceof RootNode)) {
            throw new IllegalArgumentException("RootNode instance required");
        }
        this.node = Objects.requireNonNull(node);
        this.parentPath = this;
        this.rootPath = (NodePath<RootNode>) this;
    }

    static NodePath<RootNode> forRoot(@NonNull RootNode rootNode) {
        return new NodePath<>(rootNode);
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

    public Node<?, ?> getNode() {
        return node;
    }

    public NodePath<?> getParentPath() {
        return parentPath;
    }

    public NodePath<RootNode> getRootPath() {
        return rootPath;
    }

    public boolean hasParentNodes() {
        return getNode() != getParentPath().getNode();
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

    public Stream<NodePath<?>> stream() {
        return Stream.<NodePath<?>> iterate(this, NodePath::hasParentNodes,
                NodePath::getParentPath);
    }

    @Override
    public String toString() {
        var list = new LinkedList<String>();
        var path = (NodePath<?>) this;
        list.add(path.getNode().toString());
        while (path.hasParentNodes()) {
            path = path.getParentPath();
            list.addFirst("/");
            list.addFirst(path.getNode().toString());
        }
        return String.join("", list);
    }

    <N extends Node<?, ?>> NodePath<N> withChildNode(@NonNull N node) {
        return new NodePath<>(node, this);
    }
}
