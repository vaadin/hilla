package dev.hilla.parser.plugins.model;

import java.util.List;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.core.Walker;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.plugins.backbone.AssociationMap;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;

public final class ModelPlugin implements Plugin {
    private int order = 200;
    private SharedStorage storage;

    @Override
    public void execute(List<ClassInfoModel> endpoints) {
        var associationMap = (AssociationMap) storage.getPluginStorage()
                .get(BackbonePlugin.ASSOCIATION_MAP);

        var walker = new Walker(List.of(new ModelVisitor(associationMap, 0)),
                endpoints);

        walker.traverse();
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
    public void setStorage(@Nonnull SharedStorage storage) {
        this.storage = storage;
    }
}
