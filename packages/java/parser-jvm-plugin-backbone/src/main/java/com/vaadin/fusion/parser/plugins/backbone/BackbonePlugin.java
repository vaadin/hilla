package com.vaadin.fusion.parser.plugins.backbone;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.vaadin.fusion.parser.core.Plugin;
import com.vaadin.fusion.parser.core.RelativeClassInfo;
import com.vaadin.fusion.parser.core.SharedStorage;

import io.swagger.v3.oas.models.OpenAPI;

public final class BackbonePlugin implements Plugin {
    private int order = 0;

    @Override
    public void execute(@Nonnull Collection<RelativeClassInfo> endpoints,
            @Nonnull Collection<RelativeClassInfo> entities,
            @Nonnull SharedStorage storage) {
        OpenAPI model = storage.getOpenAPI();

        new EndpointProcessor(endpoints, model).process();
        new EntityProcessor(entities, model).process();
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
