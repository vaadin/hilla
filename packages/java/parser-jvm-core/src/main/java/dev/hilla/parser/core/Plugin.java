package dev.hilla.parser.core;

import java.util.Collection;

import javax.annotation.Nonnull;

public interface Plugin {
    void execute(@Nonnull Collection<RelativeClassInfo> endpoints,
            @Nonnull Collection<RelativeClassInfo> entities,
            @Nonnull SharedStorage storage);

    int getOrder();

    void setOrder(int order);

    default void setConfig(PluginConfiguration config) {
        if (config != null) {
            throw new IllegalArgumentException(String.format(
                    "The '%s' plugin does not expect configuration set",
                    getClass().getName()));
        }
    }
}
