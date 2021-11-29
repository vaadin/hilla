package com.vaadin.fusion.parser.core;

import java.util.Collection;

import javax.annotation.Nonnull;

public interface Plugin {
    void execute(@Nonnull Collection<RelativeClassInfo> endpoints,
            @Nonnull Collection<RelativeClassInfo> entities,
            @Nonnull SharedStorage storage);
}
