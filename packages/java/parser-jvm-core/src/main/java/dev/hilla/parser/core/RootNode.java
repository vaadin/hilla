package dev.hilla.parser.core;

import javax.annotation.Nonnull;

import io.swagger.v3.oas.models.OpenAPI;

public final class RootNode extends AbstractNode<ScanResult, OpenAPI> {
    RootNode(@Nonnull ScanResult source, @Nonnull OpenAPI target) {
        super(source, target);
    }
}
