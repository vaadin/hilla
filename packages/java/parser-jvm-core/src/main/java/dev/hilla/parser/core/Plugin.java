package dev.hilla.parser.core;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

public interface Plugin extends Comparable<Plugin> {
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

    @Override
    default int compareTo(Plugin o) {
        if (runAfter().contains(o.getClass())) {
            return 1;
        }
        if (runBefore().contains(o.getClass())) {
            return -1;
        }
        if (o.runAfter().contains(getClass())) {
            return -1;
        }
        if (o.runBefore().contains(getClass())) {
            return 1;
        }
        return getClass().getName().compareTo(o.getClass().getName());
    }
}
