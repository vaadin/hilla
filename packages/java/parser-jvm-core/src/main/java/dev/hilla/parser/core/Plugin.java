package dev.hilla.parser.core;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;

import dev.hilla.parser.node.NodeDependencies;
import dev.hilla.parser.node.NodePath;

public interface Plugin {
    @Nonnull
    NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies);

    void enter(NodePath<?> nodePath);

    void exit(NodePath<?> nodePath);

    int getOrder();

    void setOrder(int order);

    PluginConfiguration getConfiguration();

    void setConfiguration(PluginConfiguration configuration);

    default Collection<Class<? extends Plugin>> getRequiredPlugins() {
        return Collections.emptyList();
    }

    void setStorage(SharedStorage storage);
}
