package dev.hilla.parser.core;

import java.util.Collection;

import javax.annotation.Nonnull;

public interface Plugin {
    int getOrder();

    void setOrder(int order);

    Collection<Visitor> getVisitors();

    default void setConfig(PluginConfiguration config) {
        if (config != null) {
            throw new IllegalArgumentException(String.format(
                    "The '%s' plugin does not expect configuration set",
                    getClass().getName()));
        }
    }

    void setStorage(@Nonnull SharedStorage storage);
}
