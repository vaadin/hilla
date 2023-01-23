package dev.hilla.parser.plugins.backbone;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.Node;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.NodePath;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.plugins.backbone.nodes.EntityNode;
import dev.hilla.parser.plugins.backbone.nodes.PropertyNode;

public final class PropertyPlugin extends AbstractPlugin<PluginConfiguration> {

    private final JacksonPropertyCollector collector;

    public PropertyPlugin(ObjectMapper mapper) {
        this.collector = new JacksonPropertyCollector(mapper);
    }

    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof PropertyNode) {
            var propertyNode = (PropertyNode) nodePath.getNode();
            propertyNode.setTarget(propertyNode.getSource().getName());
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {
    }

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        if (!(nodeDependencies.getNode() instanceof EntityNode)) {
            return nodeDependencies;
        }

        var model = (ClassInfoModel) nodeDependencies.getNode().getSource();
        if (model.isEnum()) {
            return nodeDependencies;
        }

        var properties = collector.collectFrom(model)
                .<Node<?, ?>> map(PropertyNode::of);

        return nodeDependencies.appendChildNodes(properties);
    }
}
