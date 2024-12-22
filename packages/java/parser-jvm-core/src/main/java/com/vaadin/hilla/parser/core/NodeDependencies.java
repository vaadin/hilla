package com.vaadin.hilla.parser.core;

import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

public final class NodeDependencies {
    private final Stream<Node<?, ?>> childNodes;
    private final Node<?, ?> node;
    private final Stream<Node<?, ?>> relatedNodes;

    NodeDependencies(@NonNull Node<?, ?> node,
            @NonNull Stream<Node<?, ?>> childNodes,
            @NonNull Stream<Node<?, ?>> relatedNodes) {
        this.node = Objects.requireNonNull(node);
        this.childNodes = Objects.requireNonNull(childNodes);
        this.relatedNodes = Objects.requireNonNull(relatedNodes);
    }

    @NonNull
    public NodeDependencies appendChildNodes(
            @NonNull Stream<Node<?, ?>> childNodesToAppend) {
        return withChildNodes(
                Stream.concat(getChildNodes(), childNodesToAppend));
    }

    @NonNull
    public NodeDependencies appendRelatedNodes(
            @NonNull Stream<Node<?, ?>> relatedNodesToAppend) {
        return withRelatedNodes(
                Stream.concat(getRelatedNodes(), relatedNodesToAppend));
    }

    public Stream<Node<?, ?>> getChildNodes() {
        return childNodes;
    }

    public Node<?, ?> getNode() {
        return node;
    }

    public Stream<Node<?, ?>> getRelatedNodes() {
        return relatedNodes;
    }

    @NonNull
    public NodeDependencies prependChildNodes(
            @NonNull Stream<Node<?, ?>> childNodesToPrepend) {
        return withChildNodes(
                Stream.concat(childNodesToPrepend, getChildNodes()));
    }

    @NonNull
    public NodeDependencies prependRelatedNodes(
            @NonNull Stream<Node<?, ?>> relatedNodesToPrepend) {
        return withRelatedNodes(
                Stream.concat(relatedNodesToPrepend, getRelatedNodes()));
    }

    @NonNull
    public NodeDependencies processChildNodes(
            @NonNull UnaryOperator<Stream<Node<?, ?>>> childNodesProcessor) {
        return new NodeDependencies(getNode(),
                childNodesProcessor.apply(getChildNodes()), getRelatedNodes());
    }

    @NonNull
    public NodeDependencies processRelatedNodes(
            @NonNull UnaryOperator<Stream<Node<?, ?>>> relatedNodesProcessor) {
        return new NodeDependencies(getNode(), getChildNodes(),
                relatedNodesProcessor.apply(getRelatedNodes()));
    }

    @NonNull
    private NodeDependencies withChildNodes(
            @NonNull Stream<Node<?, ?>> childNodes) {
        return new NodeDependencies(getNode(), childNodes, getRelatedNodes());
    }

    @NonNull
    private NodeDependencies withRelatedNodes(
            @NonNull Stream<Node<?, ?>> relatedNodes) {
        return new NodeDependencies(getNode(), getChildNodes(), relatedNodes);
    }

}
