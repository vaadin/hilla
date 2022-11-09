package dev.hilla.parser.core;

import jakarta.annotation.Nonnull;

import io.github.classgraph.ScanResult;
import io.swagger.v3.oas.models.OpenAPI;

public final class RootNode extends AbstractNode<ScanResult, OpenAPI> {
    RootNode(@Nonnull ScanResult source, @Nonnull OpenAPI target) {
        super(source, target);
    }
}
