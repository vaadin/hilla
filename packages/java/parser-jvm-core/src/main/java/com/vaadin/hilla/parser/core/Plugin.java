package com.vaadin.hilla.parser.core;

import java.util.Collection;
import java.util.Collections;

import org.jspecify.annotations.NonNull;

public interface Plugin {

    void enter(NodePath<?> nodePath);

    void exit(NodePath<?> nodePath);

    PluginConfiguration getConfiguration();

    void setConfiguration(PluginConfiguration configuration);

    default Collection<Class<? extends Plugin>> getRequiredPlugins() {
        return Collections.emptyList();
    }

    @NonNull
    default Node<?, ?> resolve(@NonNull Node<?, ?> node,
            @NonNull NodePath<?> parentPath) {
        return node;
    }

    @NonNull
    NodeDependencies scan(@NonNull NodeDependencies nodeDependencies);

    void setStorage(SharedStorage storage);
}
