package dev.hilla.parser.plugins.nonnull;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.PluginConfiguration;
import dev.hilla.parser.core.PluginsToolset;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.core.Visitor;
import dev.hilla.parser.plugins.backbone.AssociationMap;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.utils.PluginException;

public final class NonnullPlugin implements Plugin {
    private Collection<AnnotationMatcher> annotations = NonnullPluginConfig.Processor.defaults;
    private int order = 100;
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

        return List.of(new NonnullVisitor(annotations, associationMap,
                this::getOrder, 0));
    }

    @Override
    public void setConfig(PluginConfiguration config) {
        if (config == null) {
            return;
        }

        if (!(config instanceof NonnullPluginConfig)) {
            throw new IllegalArgumentException(String.format(
                    "Configuration for '%s' plugin should be an instance of '%s'",
                    getClass().getName(), NonnullPluginConfig.class.getName()));
        }

        annotations = new NonnullPluginConfig.Processor(
                (NonnullPluginConfig) config).process();
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
