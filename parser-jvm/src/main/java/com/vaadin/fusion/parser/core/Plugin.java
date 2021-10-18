package com.vaadin.fusion.parser.core;

import java.util.List;

import javax.annotation.Nonnull;

public interface Plugin {
    void execute(@Nonnull List<RelativeClassInfo> endpoints,
            @Nonnull List<RelativeClassInfo> entities,
            @Nonnull SharedStorage storage);
}
