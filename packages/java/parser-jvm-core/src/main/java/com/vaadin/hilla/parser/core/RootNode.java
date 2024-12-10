package com.vaadin.hilla.parser.core;

import java.util.List;

import javax.annotation.Nonnull;

import io.swagger.v3.oas.models.OpenAPI;

public final class RootNode extends AbstractNode<List<Class<?>>, OpenAPI> {
    RootNode(@Nonnull List<Class<?>> source, @Nonnull OpenAPI target) {
        super(source, target);
    }
}
