package dev.hilla.parser.plugins.backbone.nodes;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AbstractNode;
import dev.hilla.parser.models.ClassInfoModel;
import io.swagger.v3.oas.models.tags.Tag;

public final class EndpointExposedNode
        extends AbstractNode<ClassInfoModel, Tag> {
    private EndpointExposedNode(@Nonnull ClassInfoModel classInfo,
            @Nonnull Tag schema) {
        super(classInfo, schema);
    }

    @Nonnull
    public static EndpointExposedNode of(@Nonnull ClassInfoModel classInfo) {
        return new EndpointExposedNode(classInfo, new Tag());
    }
}
