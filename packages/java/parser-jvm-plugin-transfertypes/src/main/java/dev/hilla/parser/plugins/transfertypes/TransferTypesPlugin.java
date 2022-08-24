package dev.hilla.parser.plugins.transfertypes;

import java.util.List;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.PluginsToolset;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.core.Walker;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.utils.PluginException;

public final class TransferTypesPlugin implements Plugin {
    private int order = -100;

    @Override
    public void execute(List<ClassInfoModel> endpoints) {
        var visitors = List.of(new TransferTypesVisitor.PageReplacer(0),
                new TransferTypesVisitor.PageableReplacer(1),
                new TransferTypesVisitor.SortOrderReplacer(2),
                new TransferTypesVisitor.SortReplacer(3),
                new TransferTypesVisitor.UUIDReplacer(4));

        var walker = new Walker(visitors, endpoints);

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
        var toolset = new PluginsToolset(
                storage.getParserConfig().getPlugins());

        if (toolset.comparePluginOrders(this, BackbonePlugin.class)
                .map(result -> result >= 0).orElse(true)) {
            throw new PluginException(String.format(
                    "%s should be run before %s", getClass().getSimpleName(),
                    BackbonePlugin.class.getSimpleName()));
        }
    }
}
