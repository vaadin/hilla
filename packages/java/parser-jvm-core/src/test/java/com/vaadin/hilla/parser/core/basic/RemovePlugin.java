package com.vaadin.hilla.parser.core.basic;

import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.Node;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.test.nodes.EntityNode;

final class RemovePlugin extends AbstractPlugin<PluginConfiguration> {
    @Override
    public void enter(NodePath<?> nodePath) {

    }

    @Override
    public void exit(NodePath<?> nodePath) {

    }

    @NonNull
    @Override
    public NodeDependencies scan(@NonNull NodeDependencies nodeDependencies) {
        return nodeDependencies.processRelatedNodes(this::removeBazEntity);
    }

    private Stream<Node<?, ?>> removeBazEntity(Stream<Node<?, ?>> nodes) {
        return nodes.filter(
                node -> !((node instanceof EntityNode) && ((EntityNode) node)
                        .getSource().getSimpleName().equals("Baz")));
    }
}
