package dev.hilla.parser.plugins.backbone.classmappers;

import java.util.Collection;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.models.ClassInfoModel;

public class ClassMappersPlugin implements Plugin.Processor {
    private int order = -100;
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
        storage.getClassMappers()
                .add(model -> ClassInfoModel
                        .isAssignableFrom(Replace.From.class, model)
                                ? ClassInfoModel.of(Replace.To.class)
                                : model);
    }

    @Override
    public void setStorage(@Nonnull SharedStorage storage) {
        this.storage = storage;
    }
}
