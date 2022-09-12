package dev.hilla.parser.core.basic;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import dev.hilla.parser.core.AbstractPlugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.Model;
import dev.hilla.parser.models.NamedModel;
import dev.hilla.parser.node.NodeDependencies;
import dev.hilla.parser.node.NodePath;
import dev.hilla.parser.node.RootNode;

final class FinalizePlugin extends AbstractPlugin<PluginConfiguration> {
    private final List<String> footsteps = new ArrayList<>();
    private final List<String> members = new ArrayList<>();

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        return nodeDependencies;
    }

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
            getStorage().getPluginStorage().put(BasicPlugin.STORAGE_KEY,
                    members);
            getStorage().getPluginStorage().put(
                    BasicPlugin.FOOTSTEPS_STORAGE_KEY,
                    String.join("\n", footsteps));
        }
    }
}
