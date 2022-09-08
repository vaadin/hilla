package dev.hilla.parser.core.basic;

import javax.annotation.Nonnull;

import java.util.stream.Stream;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.node.EntityNode;
import dev.hilla.parser.node.Node;
import dev.hilla.parser.node.NodeDependencies;

final class RemovePlugin extends AbstractPlugin<PluginConfiguration> {
    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        return NodeDependencies.of(nodeDependencies.getNode(),
            nodeDependencies.getChildNodes(),
            removeBazEntity(nodeDependencies.getRelatedNodes()));
    }

    @Override
    public void enter(dev.hilla.parser.node.NodePath<?> nodePath) {

    }

    @Override
    public void exit(dev.hilla.parser.node.NodePath<?> nodePath) {

    }

    private Stream<Node<?, ?>> removeBazEntity(Stream<Node<?, ?>> nodes) {
        return nodes.filter(node -> !((node instanceof EntityNode) &&
                                          ((EntityNode) node).getSource()
                                              .getSimpleName().equals("Baz")));
    }
}
