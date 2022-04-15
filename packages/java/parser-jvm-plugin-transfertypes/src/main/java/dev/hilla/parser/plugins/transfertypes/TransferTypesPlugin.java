package dev.hilla.parser.plugins.transfertypes;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.PluginsToolset;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.utils.PluginException;

public final class TransferTypesPlugin implements Plugin.Processor {
    private final List<Replacer> replacers = List.of(new PageableReplacer(),
            new UUIDReplacer());
    private int order = -100;

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
        for (var replacer : replacers) {
            replacer.process();
        }
    }

    @Override
    public void setStorage(@Nonnull SharedStorage storage) {
        var toolset = new PluginsToolset(
                storage.getParserConfig().getPlugins());

        if (toolset.comparePluginOrders(this, BackbonePlugin.class)
                .map(result -> result >= 0).orElse(true)) {
            throw new PluginException(
                    "TransferTypesPlugin should be run before BackbonePlugin");
        }

        var classMappers = storage.getClassMappers();

        for (var replacer : replacers) {
            replacer.setClassMappers(classMappers);
        }
    }
}
