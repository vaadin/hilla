package dev.hilla.parser.core.basic;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.node.EndpointNode;
import dev.hilla.parser.node.FieldNode;
import dev.hilla.parser.node.MethodNode;
import dev.hilla.parser.node.Node;
import dev.hilla.parser.node.NodeDependencies;
import dev.hilla.parser.node.NodePath;

final class ReplacePlugin extends AbstractPlugin<PluginConfiguration> {
    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        var node = nodeDependencies.getNode();
        if (node instanceof EndpointNode) {
            return NodeDependencies.of(node,
                    Stream.concat(
                            removeBarMethod(nodeDependencies.getChildNodes()),
                            getReplacementFields()),
                    nodeDependencies.getRelatedNodes());
        } else {
            return nodeDependencies;
        }
    }

    @Override
    public void enter(NodePath<?> nodePath) {

    }

    @Override
    public void exit(NodePath<?> nodePath) {

    }

    @Nonnull
    private Stream<Node<?, ?>> removeBarMethod(
            @Nonnull Stream<Node<?, ?>> nodes) {
        return nodes.filter(node -> !((node instanceof MethodNode)
                && ((MethodNode) node).getSource().getName().equals("bar")));
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

    static class Sample {
        private final String fieldBar = "bar";
        private final String fieldFoo = "foo";
    }
}
