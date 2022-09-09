package dev.hilla.parser.plugins.backbone;

import javax.annotation.Nonnull;

import java.util.stream.Stream;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.node.EndpointNode;
import dev.hilla.parser.node.NodeDependencies;
import dev.hilla.parser.node.NodePath;
import dev.hilla.parser.node.RootNode;
import io.swagger.v3.oas.models.tags.Tag;

public class EndpointPlugin extends AbstractPlugin<PluginConfiguration> {
    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        if (nodeDependencies.getNode() instanceof RootNode) {
            var rootNode = (RootNode) nodeDependencies.getNode();
            var endpointAnnotationName = getStorage().getParserConfig()
                .getEndpointAnnotationName();
            var endpoints = rootNode.getSource()
                .getClassesWithAnnotation(endpointAnnotationName);
            return NodeDependencies.of(rootNode,
                endpoints.stream().map(ClassInfoModel::of)
                    .filter(ClassInfoModel::isNonJDKClass)
                    .map(EndpointNode::of), Stream.empty());
        }
        return nodeDependencies;
    }

    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof EndpointNode) {
            var endpointNode = (EndpointNode) nodePath.getNode();
            var name = endpointNode.getSource().getSimpleName();
            endpointNode.setTarget(new Tag().name(name));
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        var node = nodePath.getNode();
        var parentNode = nodePath.getParentPath().getNode();
        if (node instanceof EndpointNode && parentNode instanceof RootNode) {
            ((RootNode) parentNode).getTarget()
                .addTagsItem(((EndpointNode) node).getTarget());
        }
    }
}
