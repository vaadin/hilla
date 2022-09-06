package dev.hilla.parser.node;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.stream.Stream;

public final class NodeDependencies {
    private final Node<?, ?> node;
    private final Stream<NodePath> dependencies;

    public NodeDependencies(@Nonnull Node<?, ?> node,
        @Nonnull Stream<NodePath> dependencies) {
        this.node = Objects.requireNonNull(node);
        this.dependencies = Objects.requireNonNull(dependencies);
    }

    public Node<?, ?> getNode() {
        return node;
    }

    public Stream<NodePath> getDependencies() {
        return dependencies;
    }
}
