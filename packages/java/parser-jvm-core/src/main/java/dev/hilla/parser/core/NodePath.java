package dev.hilla.parser.core;

import dev.hilla.parser.models.AnnotatedModel;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.ClassInfoModel;

import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public final class NodePath<N extends Node<?, ?>> {
    private final N node;
    private final NodePath<?> parentPath;
    private final NodePath<RootNode> rootPath;

    private NodePath(@Nonnull N node, @Nonnull NodePath<?> parentPath) {
        this.node = Objects.requireNonNull(node);
        this.parentPath = Objects.requireNonNull(parentPath);
        this.rootPath = Objects.requireNonNull(parentPath.getRootPath());
    }

    @SuppressWarnings("unchecked")
    private NodePath(@Nonnull N node) {
        if (!(node instanceof RootNode)) {
            throw new IllegalArgumentException("RootNode instance required");
        }
        this.node = Objects.requireNonNull(node);
        this.parentPath = this;
        this.rootPath = (NodePath<RootNode>) this;
    }

    static NodePath<RootNode> forRoot(@Nonnull RootNode rootNode) {
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

    <N extends Node<?, ?>> NodePath<N> childPath(@Nonnull N node) {
        return new NodePath<>(node, this);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Stream<Node<?, ?>> getParentNodes() {
        return Stream.iterate((NodePath) this, NodePath::hasParentNodes,
                NodePath::getParentPath).map(NodePath::getNode);
    }

    public Stream<AnnotationInfoModel> getAnnotations() {
        var models = getParentNodes()
                .filter(node -> node.getSource() instanceof AnnotatedModel)
                .map(node -> (AnnotatedModel) node.getSource());
        return models.flatMap(AnnotatedModel::getAnnotationsStream);
    }

    public Stream<AnnotationInfoModel> getPackageAnnotations() {
        var classes = getParentNodes()
                .filter(node -> node.getSource() instanceof ClassInfoModel)
                .map(node -> (ClassInfoModel) node.getSource());
        return classes.map(ClassInfoModel::getPackage)
                .flatMap(AnnotatedModel::getAnnotationsStream);
    }

}
