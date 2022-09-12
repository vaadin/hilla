package dev.hilla.parser.node;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.stream.Stream;

public final class NodeDependencies {
    private final Node<?, ?> node;
    private final Stream<Node<?, ?>> childNodes;
    private final Stream<Node<?, ?>> relatedNodes;

    private NodeDependencies(@Nonnull Node<?, ?> node,
            @Nonnull Stream<Node<?, ?>> childNodes,
            @Nonnull Stream<Node<?, ?>> relatedNodes) {
        this.node = Objects.requireNonNull(node);
        this.childNodes = Objects.requireNonNull(childNodes);
        this.relatedNodes = Objects.requireNonNull(relatedNodes);
    }

    public Node<?, ?> getNode() {
        return node;
    }

    public Stream<Node<?, ?>> getChildNodes() {
        return childNodes;
    }

    public Stream<Node<?, ?>> getRelatedNodes() {
        return relatedNodes;
    }

    static public NodeDependencies of(Node<?, ?> node,
            Stream<Node<?, ?>> childNodes, Stream<Node<?, ?>> relatedNodes) {
        return new NodeDependencies(node, childNodes, relatedNodes);
    }
}
