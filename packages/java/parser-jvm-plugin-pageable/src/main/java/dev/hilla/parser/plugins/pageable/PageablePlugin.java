package dev.hilla.parser.plugins.pageable;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.MappingRuleSet;
import dev.hilla.parser.core.Plugin;
import dev.hilla.parser.core.PluginsToolset;
import dev.hilla.parser.core.SharedStorage;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.plugins.backbone.BackbonePlugin;
import dev.hilla.parser.utils.PluginException;
import dev.hilla.runtime.transfertypes.Order;
import dev.hilla.runtime.transfertypes.Pageable;
import dev.hilla.runtime.transfertypes.Sort;

public class PageablePlugin implements Plugin.Processor {
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
        var map = storage.getMappingRuleSet();

        map.add(MappingRuleSet.createReplacer(
                "org.springframework.data.domain.Sort",
                ClassInfoModel.of(Sort.class)));
        map.add(MappingRuleSet.createReplacer(
                "org.springframework.data.domain.Pageable",
                ClassInfoModel.of(Pageable.class)));
        map.add(MappingRuleSet.createReplacer(
                "org.springframework.data.domain.Page",
                ClassInfoModel.of(List.class)));
        map.add(MappingRuleSet.createReplacer(
                "org.springframework.data.domain.Sort$Order",
                ClassInfoModel.of(Order.class)));
    }

    @Override
    public void setStorage(@Nonnull SharedStorage storage) {
        var toolset = new PluginsToolset(
                storage.getParserConfig().getPlugins());

        if (toolset.comparePluginOrders(this, BackbonePlugin.class)
                .map(result -> result >= 0).orElse(true)) {
            throw new PluginException(
                    "PageablePlugin should be run before BackbonePlugin");
        }

        this.storage = storage;
    }
}
