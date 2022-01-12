package com.vaadin.fusion.parser.plugins.nonnull;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.vaadin.fusion.parser.core.Plugin;
import com.vaadin.fusion.parser.core.PluginConfiguration;
import com.vaadin.fusion.parser.core.RelativeClassInfo;
import com.vaadin.fusion.parser.core.SharedStorage;
import com.vaadin.fusion.parser.plugins.backbone.AssociationMap;
import com.vaadin.fusion.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.fusion.parser.utils.PluginException;

public final class NonnullPlugin implements Plugin {
    private Collection<String> annotations;
    private int order = 100;

    @Override
    public void execute(@Nonnull Collection<RelativeClassInfo> endpoints,
            @Nonnull Collection<RelativeClassInfo> entities,
            @Nonnull SharedStorage storage) {
        var associationMap = (AssociationMap) storage.getPluginStorage()
                .get(BackbonePlugin.ASSOCIATION_MAP);

        if (associationMap == null) {
            throw new PluginException(
                    "NonnullPlugin should be run after BackbonePlugin");
        }

        new NonnullProcessor(annotations, associationMap).process();
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
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
}
