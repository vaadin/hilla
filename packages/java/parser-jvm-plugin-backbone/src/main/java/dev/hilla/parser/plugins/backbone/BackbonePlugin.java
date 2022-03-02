package dev.hilla.parser.plugins.backbone;

import java.util.Collection;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.models.ClassInfoModel;

public final class BackbonePlugin implements Plugin.Processor {
    private int order = 0;
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
    public void process(@Nonnull Collection<ClassInfoModel> endpoints,
            @Nonnull Collection<ClassInfoModel> entities) {
        var model = storage.getOpenAPI();
        var context = new Context(storage.getAssociationMap());

        new EndpointProcessor(endpoints, model, context).process();
        new EntityProcessor(entities, model, context).process();
    }

    @Override
    public void setStorage(@Nonnull SharedStorage storage) {
        this.storage = storage;
    }
}
