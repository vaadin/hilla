package com.vaadin.hilla.parser.core.basic;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.Node;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.models.FieldInfoModel;
import com.vaadin.hilla.parser.test.nodes.EndpointNode;
import com.vaadin.hilla.parser.test.nodes.FieldNode;
import com.vaadin.hilla.parser.test.nodes.MethodNode;

final class ReplacePlugin extends AbstractPlugin<PluginConfiguration> {
    @Override
    public void enter(NodePath<?> nodePath) {

    }

    @Override
    public void exit(NodePath<?> nodePath) {

    }

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        var node = nodeDependencies.getNode();
        if (node instanceof EndpointNode) {
            return nodeDependencies.processChildNodes(this::removeBarMethod)
                    .appendChildNodes(getReplacementFields());
        } else {
            return nodeDependencies;
        }
    }

    @Nonnull
    private Stream<Node<?, ?>> getReplacementFields() {
        try {
            return Stream
                    .of(Sample.class.getDeclaredField("fieldFoo"),
                            Sample.class.getDeclaredField("fieldBar"))
                    .map(FieldInfoModel::of).map(FieldNode::of);
        } catch (NoSuchFieldException e) {
            return Stream.empty();
        }
    }

    @Nonnull
    private Stream<Node<?, ?>> removeBarMethod(
            @Nonnull Stream<Node<?, ?>> nodes) {
        return nodes.filter(node -> !((node instanceof MethodNode)
                && ((MethodNode) node).getSource().getName().equals("bar")));
    }

    static class Sample {
        private final String fieldBar = "bar";
        private final String fieldFoo = "foo";
    }
}
