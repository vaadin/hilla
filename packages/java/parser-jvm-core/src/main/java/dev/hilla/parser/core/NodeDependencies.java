package dev.hilla.parser.core;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class NodeDependencies {
    private final Node<?, ?> node;
    private final Stream<Node<?, ?>> childNodes;
    private final Stream<Node<?, ?>> relatedNodes;

    NodeDependencies(@Nonnull Node<?, ?> node,
            @Nonnull Stream<Node<?, ?>> childNodes,
            @Nonnull Stream<Node<?, ?>> relatedNodes) {
        this.node = Objects.requireNonNull(node);
        this.childNodes = Objects.requireNonNull(childNodes);
        this.relatedNodes = Objects.requireNonNull(relatedNodes);
    }

    public Node<?, ?> getNode() {
        return node;
    }

    @Nonnull
    public NodeDependencies processChildNodes(
            @Nonnull UnaryOperator<Stream<Node<?, ?>>> childNodesProcessor) {
        return new NodeDependencies(getNode(),
                childNodesProcessor.apply(getChildNodes()), getRelatedNodes());
    }

    @Nonnull
    public NodeDependencies processRelatedNodes(
            @Nonnull UnaryOperator<Stream<Node<?, ?>>> relatedNodesProcessor) {
        return new NodeDependencies(getNode(), getChildNodes(),
                relatedNodesProcessor.apply(getRelatedNodes()));
    }

    @Nonnull
    public NodeDependencies appendChildNodes(
            @Nonnull Stream<Node<?, ?>> childNodesToAppend) {
        return withChildNodes(
                Stream.concat(getChildNodes(), childNodesToAppend));
    }

    @Nonnull
    public NodeDependencies appendRelatedNodes(
            @Nonnull Stream<Node<?, ?>> relatedNodesToAppend) {
        return withRelatedNodes(
                Stream.concat(getRelatedNodes(), relatedNodesToAppend));
    }

    @Nonnull
    public NodeDependencies prependChildNodes(
            @Nonnull Stream<Node<?, ?>> childNodesToPrepend) {
        return withChildNodes(
                Stream.concat(childNodesToPrepend, getChildNodes()));
    }

    @Nonnull
    public NodeDependencies prependRelatedNodes(
            @Nonnull Stream<Node<?, ?>> relatedNodesToPrepend) {
        return withRelatedNodes(
                Stream.concat(relatedNodesToPrepend, getRelatedNodes()));
    }

    @Nonnull
    private NodeDependencies withChildNodes(
            @Nonnull Stream<Node<?, ?>> childNodes) {
        return new NodeDependencies(getNode(), childNodes, getRelatedNodes());
    }

    @Nonnull
    private NodeDependencies withRelatedNodes(
            @Nonnull Stream<Node<?, ?>> relatedNodes) {
        return new NodeDependencies(getNode(), getChildNodes(), relatedNodes);
    }

    public Stream<Node<?, ?>> getChildNodes() {
        return childNodes;
    }

    public Stream<Node<?, ?>> getRelatedNodes() {
        return relatedNodes;
    }

}
