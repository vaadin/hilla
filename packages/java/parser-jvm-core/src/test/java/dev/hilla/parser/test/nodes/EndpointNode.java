package dev.hilla.parser.test.nodes;

import jakarta.annotation.Nonnull;

import dev.hilla.parser.core.AbstractNode;
import dev.hilla.parser.models.ClassInfoModel;
import io.swagger.v3.oas.models.tags.Tag;

public final class EndpointNode extends AbstractNode<ClassInfoModel, Tag> {
    private EndpointNode(@Nonnull ClassInfoModel value, @Nonnull Tag schema) {
        super(value, schema);
    }

    @Nonnull
    public static EndpointNode of(@Nonnull ClassInfoModel cls) {
        return new EndpointNode(cls, new Tag());
    }
}
