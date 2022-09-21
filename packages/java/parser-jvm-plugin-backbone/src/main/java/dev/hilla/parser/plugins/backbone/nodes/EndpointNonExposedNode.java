package dev.hilla.parser.plugins.backbone.nodes;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AbstractNode;
import dev.hilla.parser.models.ClassInfoModel;
import io.swagger.v3.oas.models.tags.Tag;

public final class EndpointNonExposedNode extends AbstractNode<ClassInfoModel, Tag> {
    private EndpointNonExposedNode(@Nonnull ClassInfoModel value, @Nonnull Tag schema) {
        super(value, schema);
    }

    @Nonnull
    public static EndpointNonExposedNode of(@Nonnull ClassInfoModel cls) {
        return new EndpointNonExposedNode(cls, new Tag());
    }
}
