package dev.hilla.parser.plugins.model;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.PluginsToolset;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.plugins.backbone.AssociationMap;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.utils.PluginException;

public final class ModelPlugin implements Plugin {
    private int order = 200;
    private SharedStorage storage;

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public Collection<Visitor> getVisitors() {
        var associationMap = (AssociationMap) storage.getPluginStorage()
                .get(BackbonePlugin.ASSOCIATION_MAP);

        return List.of(new ModelVisitor(associationMap, this::getOrder, 0));
    }

    @Override
    public void setStorage(@Nonnull SharedStorage storage) {
        var toolset = new PluginsToolset(
                storage.getParserConfig().getPlugins());

        if (toolset.comparePluginOrders(this, BackbonePlugin.class)
                .map(result -> result <= 0).orElse(true)) {
            throw new PluginException(String.format("%s should be run after %s",
                    getClass().getSimpleName(),
                    BackbonePlugin.class.getSimpleName()));
        }

        this.storage = storage;
    }
}
