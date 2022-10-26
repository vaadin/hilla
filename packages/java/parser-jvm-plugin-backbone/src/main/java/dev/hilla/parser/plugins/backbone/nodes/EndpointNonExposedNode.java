package dev.hilla.parser.plugins.backbone.nodes;

import jakarta.annotation.Nonnull;

import dev.hilla.parser.core.AbstractNode;
import dev.hilla.parser.models.ClassInfoModel;
import io.swagger.v3.oas.models.tags.Tag;

public final class EndpointNonExposedNode
        extends AbstractNode<ClassInfoModel, Void> {
    private EndpointNonExposedNode(@Nonnull ClassInfoModel classInfo) {
        super(classInfo, null);
    }

    @Nonnull
    public static EndpointNonExposedNode of(@Nonnull ClassInfoModel classInfo) {
        return new EndpointNonExposedNode(classInfo);
    }
}
