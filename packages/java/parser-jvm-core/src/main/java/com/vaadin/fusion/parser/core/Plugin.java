package com.vaadin.fusion.parser.core;

import java.util.Collection;

import javax.annotation.Nonnull;

public interface Plugin extends Comparable<Plugin> {
    @Override
    default int compareTo(@Nonnull Plugin plugin) {
        return Integer.compare(getOrder(), plugin.getOrder());
    }

    void execute(@Nonnull Collection<RelativeClassInfo> endpoints,
            @Nonnull Collection<RelativeClassInfo> entities,
            @Nonnull SharedStorage storage);

    int getOrder();

    default void setConfig(PluginConfiguration config) {
        if (config != null) {
            throw new IllegalArgumentException(String.format(
                    "The '%s' plugin does not expect configuration set",
                    getClass().getName()));
        }
    }

    void setOrder(int order);
}
