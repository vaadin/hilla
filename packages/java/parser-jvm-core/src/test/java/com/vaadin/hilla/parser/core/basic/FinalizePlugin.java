package com.vaadin.hilla.parser.core.basic;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.core.RootNode;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import com.vaadin.hilla.parser.models.Model;
import com.vaadin.hilla.parser.models.NamedModel;

final class FinalizePlugin extends AbstractPlugin<PluginConfiguration> {
    private final List<String> footsteps = new ArrayList<>();
    private final List<String> members = new ArrayList<>();

    @Override
    public void enter(NodePath<?> nodePath) {
        if (nodePath.getNode().getSource() instanceof Model
                && nodePath.getNode().getSource() instanceof NamedModel
                && nodePath.getParentPath().getNode()
                        .getSource() instanceof ClassInfoModel) {
            var model = nodePath.getNode().getSource();
            members.add(String.format("%s %s",
                    ((Model) model).getCommonModelClass().getSimpleName(),
                    ((NamedModel) model).getName()));
        }
        footsteps.add(String.format("-> %s", nodePath.toString()));
    }

    @Override
    public void exit(NodePath<?> nodePath) {
        footsteps.add(String.format("<- %s", nodePath.toString()));
        if (nodePath.getNode() instanceof RootNode) {
            var rootNode = (RootNode) nodePath.getNode();
            var openApi = rootNode.getTarget();
            openApi.addExtension(BasicPlugin.STORAGE_KEY,
                    String.join(", ", members));
            openApi.addExtension(BasicPlugin.FOOTSTEPS_STORAGE_KEY,
                    String.join("\n", footsteps));
        }
    }

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }
}
