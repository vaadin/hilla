package dev.hilla.parser.plugins.backbone;

import java.util.Collection;
import java.util.stream.Collectors;

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
        var replaceMap = storage.getReplaceMap();
        var context = new Context(storage.getAssociationMap(), replaceMap);

        new EndpointProcessor(
                replaceMap.process(endpoints).collect(Collectors.toList()),
                model, context).process();
        new EntityProcessor(
                replaceMap.process(entities).collect(Collectors.toList()),
                model, context).process();
    }

    @Override
    public void setStorage(@Nonnull SharedStorage storage) {
        this.storage = storage;
    }
}
