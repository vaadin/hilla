package com.vaadin.hilla.parser.core;

import java.util.List;

import org.jspecify.annotations.NonNull;

import io.swagger.v3.oas.models.OpenAPI;

public final class RootNode extends AbstractNode<List<Class<?>>, OpenAPI> {
    RootNode(@NonNull List<Class<?>> source, @NonNull OpenAPI target) {
        super(source, target);
    }
}
