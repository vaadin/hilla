package dev.hilla.parser.plugins.backbone.nodes;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AbstractNode;
import dev.hilla.parser.models.ClassInfoModel;
import io.swagger.v3.oas.models.tags.Tag;

public final class EndpointExposedNode
        extends AbstractNode<ClassInfoModel, Void> {
    private EndpointExposedNode(@Nonnull ClassInfoModel classInfo) {
        super(classInfo, null);
    }

    @Nonnull
    public static EndpointExposedNode of(@Nonnull ClassInfoModel classInfo) {
        return new EndpointExposedNode(classInfo);
    }
}
