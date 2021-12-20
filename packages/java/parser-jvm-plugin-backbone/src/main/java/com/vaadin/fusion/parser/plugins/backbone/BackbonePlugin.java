package com.vaadin.fusion.parser.plugins.backbone;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.vaadin.fusion.parser.core.Plugin;
import com.vaadin.fusion.parser.core.RelativeClassInfo;
import com.vaadin.fusion.parser.core.SharedStorage;

public final class BackbonePlugin implements Plugin {
    public static final String ASSOCIATION_MAP = "BackbonePlugin/AssociationMap";
    private int order = 0;

    @Override
    public void execute(@Nonnull Collection<RelativeClassInfo> endpoints,
            @Nonnull Collection<RelativeClassInfo> entities,
            @Nonnull SharedStorage storage) {
        var model = storage.getOpenAPI();
        var map = new AssociationMap();

        new EndpointProcessor(endpoints, model, map).process();
        new EntityProcessor(entities, model, map).process();
        storage.getPluginStorage().put(ASSOCIATION_MAP, map);
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
