package dev.hilla.parser.plugins.backbone.nodes;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AbstractNode;
import dev.hilla.parser.models.jackson.JacksonPropertyModel;

public final class PropertyNode
        extends AbstractNode<JacksonPropertyModel, String> {
    private PropertyNode(@Nonnull JacksonPropertyModel source,
            @Nonnull String target) {
        super(source, target);
    }

    @Nonnull
    static public PropertyNode of(@Nonnull JacksonPropertyModel source) {
        return new PropertyNode(source, "");
    }
}
