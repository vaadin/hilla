package com.vaadin.hilla.parser.plugins.signals;

import com.vaadin.hilla.parser.core.AbstractPlugin;
import com.vaadin.hilla.parser.core.Node;
import com.vaadin.hilla.parser.core.NodeDependencies;
import com.vaadin.hilla.parser.core.NodePath;
import com.vaadin.hilla.parser.core.Plugin;
import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.List;

public class SharedSignalsPlugin extends AbstractPlugin<PluginConfiguration> {

    @Override
    public void enter(NodePath<?> nodePath) {

    }

    @Override
    public void exit(NodePath<?> nodePath) {

    }

    @Nonnull
    @Override
    public Node<?, ?> resolve(@Nonnull Node<?, ?> node,
            @Nonnull NodePath<?> parentPath) {
        return super.resolve(node, parentPath);
    }

    @Nonnull
    @Override
    public NodeDependencies scan(@Nonnull NodeDependencies nodeDependencies) {
        return null;
    }

    @Override
    public Collection<Class<? extends Plugin>> getRequiredPlugins() {
        return List.of(BackbonePlugin.class);
    }
}
