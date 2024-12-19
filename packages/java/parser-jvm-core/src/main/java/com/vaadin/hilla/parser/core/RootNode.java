package com.vaadin.hilla.parser.core;

import org.jspecify.annotations.NonNull;

import io.swagger.v3.oas.models.OpenAPI;

public final class RootNode extends AbstractNode<ScanResult, OpenAPI> {
    RootNode(@NonNull ScanResult source, @NonNull OpenAPI target) {
        super(source, target);
    }
}
