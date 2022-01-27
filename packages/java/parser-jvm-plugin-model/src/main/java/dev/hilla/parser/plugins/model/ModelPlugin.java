package dev.hilla.parser.plugins.model;

import java.util.Collection;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.PluginsToolset;
import dev.hilla.parser.core.RelativeClassInfo;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.utils.PluginException;

public final class ModelPlugin implements Plugin {
    private int order = 100;

    @Override
    public void execute(@Nonnull Collection<RelativeClassInfo> endpoints,
            @Nonnull Collection<RelativeClassInfo> entities,
            @Nonnull SharedStorage storage) {
        var toolset = new PluginsToolset(
                storage.getParserConfig().getPlugins());

        if (toolset.comparePluginOrders(this, BackbonePlugin.class)
                .map(result -> result <= 0).orElse(true)) {
            throw new PluginException(
                    "ModelPlugin should be run after BackbonePlugin");
        }

        new ValidationConstraint.Processor(storage.getAssociationMap())
                .process();
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }
}
