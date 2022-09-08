package dev.hilla.parser.node;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.core.SharedStorage;
import io.github.classgraph.ScanResult;
import io.swagger.v3.oas.models.OpenAPI;

public final class RootNode extends AbstractNode<ScanResult, OpenAPI> {
    public RootNode(@Nonnull ScanResult source,
        @Nonnull OpenAPI target) {
        super(source, target);
    }
}
