package dev.hilla.parser.plugins.backbone;

import javax.annotation.Nonnull;

import java.util.function.Predicate;
import java.util.stream.Stream;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.node.EntityNode;
import dev.hilla.parser.node.FieldNode;
import dev.hilla.parser.node.NodeDependencies;
import dev.hilla.parser.node.NodePath;

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
            .map(FieldNode::of);

        return NodeDependencies.of(nodeDependencies.getNode(),
            Stream.concat(nodeDependencies.getChildNodes(), fields),
            Stream.empty());
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
