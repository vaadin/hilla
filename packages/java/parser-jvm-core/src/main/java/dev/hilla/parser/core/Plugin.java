package dev.hilla.parser.core;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

public interface Plugin {
    void enter(NodePath<?> nodePath);

    void exit(NodePath<?> nodePath);

    PluginConfiguration getConfiguration();

    void setConfiguration(PluginConfiguration configuration);

    default Collection<Class<? extends Plugin>> runAfter() {
        return Collections.emptyList();
    }

    default Collection<Class<? extends Plugin>> runBefore() {
        return Collections.emptyList();
    }

    @Nonnull
    NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies);

    void setStorage(SharedStorage storage);
}
