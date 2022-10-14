package dev.hilla.parser.plugins.backbone;

import jakarta.annotation.Nonnull;

import java.util.function.Predicate;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.plugins.backbone.nodes.EntityNode;
import dev.hilla.parser.plugins.backbone.nodes.FieldNode;
import dev.hilla.parser.core.NodeDependencies;
import dev.hilla.parser.core.Node;
import dev.hilla.parser.core.NodePath;

public final class FieldPlugin extends AbstractPlugin<PluginConfiguration> {
    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        if (!(nodeDependencies.getNode() instanceof EntityNode)) {
            return nodeDependencies;
        }

        var cls = (ClassInfoModel) nodeDependencies.getNode().getSource();
        if (cls.isEnum()) {
            return nodeDependencies;
        }

        var fields = cls.getFieldsStream()
                .filter(Predicate.not(FieldInfoModel::isTransient))
                .<Node<?, ?>> map(FieldNode::of);

        return nodeDependencies.appendChildNodes(fields);
    }

    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode() instanceof FieldNode) {
            var fieldNode = (FieldNode) nodePath.getNode();
            fieldNode.setTarget(fieldNode.getSource().getName());
        }
    }

    @Override
    public void exit(NodePath<?> nodePath) {
    }
}
