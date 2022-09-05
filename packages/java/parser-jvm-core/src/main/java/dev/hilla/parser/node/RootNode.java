package dev.hilla.parser.node;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.ParserConfig;
import io.swagger.v3.oas.models.OpenAPI;

public final class RootNode extends NodeImpl<ParserConfig, OpenAPI> {
    public RootNode(@Nonnull ParserConfig source,
        @Nonnull OpenAPI target) {
        super(source, target);
    }
}
