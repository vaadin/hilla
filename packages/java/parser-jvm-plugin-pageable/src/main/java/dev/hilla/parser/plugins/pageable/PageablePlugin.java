package dev.hilla.parser.plugins.pageable;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.PluginsToolset;
import dev.hilla.parser.core.RelativeClassInfo;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.utils.PluginException;

public class PageablePlugin implements Plugin.Transformer {
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
    public void setStorage(@Nonnull SharedStorage storage) {
        this.storage = storage;
    }

    @Nonnull
    @Override
    public Stream<RelativeClassInfo> transform(@Nonnull Stream<RelativeClassInfo> stream) {
        var toolset = new PluginsToolset(
            storage.getParserConfig().getPlugins());

        if (toolset.comparePluginOrders(this, BackbonePlugin.class)
            .map(result -> result >= 0).orElse(true)) {
            throw new PluginException(
                "PageablePlugin should be run before BackbonePlugin");
        }

        return new PageableTransformer(storage).transform(stream);
    }
}
