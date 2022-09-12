package dev.hilla.parser.core.basic;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.node.EndpointNode;
import dev.hilla.parser.node.EntityNode;
import dev.hilla.parser.node.FieldNode;
import dev.hilla.parser.node.MethodNode;
import dev.hilla.parser.node.Node;
import dev.hilla.parser.node.NodeDependencies;
import dev.hilla.parser.node.NodePath;
import dev.hilla.parser.node.RootNode;

final class AddPlugin extends AbstractPlugin<PluginConfiguration> {
    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        var node = nodeDependencies.getNode();
        if (node instanceof RootNode) {
            var rootNode = (RootNode) node;
            var endpoints = rootNode.getSource()
                    .getClassesWithAnnotation(getStorage().getParserConfig()
                            .getEndpointAnnotationName())
                    .stream().map(ClassInfoModel::of)
                    .collect(Collectors.toList());
            return NodeDependencies.of(node,
                    endpoints.stream().map(EndpointNode::of),
                    endpoints.stream()
                            .flatMap(ClassInfoModel::getInnerClassesStream)
                            .map(EntityNode::of));
        } else if (node instanceof EndpointNode) {
            return NodeDependencies.of(node,
                    Stream.concat(((EndpointNode) node).getSource()
                            .getFieldsStream().map(FieldNode::of),
                            ((EndpointNode) node).getSource().getMethodsStream()
                                    .map(MethodNode::of)),
                    Stream.of(EntityNode.of(ClassInfoModel.of(Sample.class))));
        } else if (node instanceof EntityNode && ((EntityNode) node).getSource()
                .getName().equals(Sample.class.getName())) {
            try {
                return NodeDependencies.of(
                        node, Stream
                                .of(Sample.class.getDeclaredMethod("methodFoo"),
                                        Sample.class
                                                .getDeclaredMethod("methodBar"))
                                .map(MethodInfoModel::of).map(MethodNode::of),
                        nodeDependencies.getRelatedNodes());
            } catch (NoSuchMethodException e) {
                return nodeDependencies;
            }
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

    static class Sample {
        public void methodBar() {
        }

        public void methodFoo() {
        }
    }
}
