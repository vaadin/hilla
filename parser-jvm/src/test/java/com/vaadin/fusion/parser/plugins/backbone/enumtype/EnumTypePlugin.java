package com.vaadin.fusion.parser.plugins.backbone.enumtype;

import java.util.List;

import javax.annotation.Nonnull;

import com.vaadin.fusion.parser.core.RelativeClassInfo;
import com.vaadin.fusion.parser.core.SharedStorage;
import com.vaadin.fusion.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.fusion.parser.testutils.PluginElementsFilter;

public class EnumTypePlugin extends BackbonePlugin {
    private final PluginElementsFilter filter = new PluginElementsFilter(
        "Enum");

    @Override
    public void execute(@Nonnull List<RelativeClassInfo> endpoints,
                        @Nonnull List<RelativeClassInfo> entities,
                        @Nonnull SharedStorage storage) {
        super.execute(filter.apply(endpoints), filter.apply(entities), storage);
    }
}
