package com.vaadin.fusion.parser.core;

import java.util.Collection;

import javax.annotation.Nonnull;

public interface Plugin {
    default void setConfig(Object config) {
        if (config != null) {
            throw new IllegalArgumentException(String.format(
                    "The '%s' plugin does not expect configuration set", getClass().getName()));
        }
    }

    void execute(@Nonnull Collection<RelativeClassInfo> endpoints,
            @Nonnull Collection<RelativeClassInfo> entities,
            @Nonnull SharedStorage storage);
}
