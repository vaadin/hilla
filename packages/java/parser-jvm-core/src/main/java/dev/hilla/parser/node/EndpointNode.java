package dev.hilla.parser.node;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.ClassInfoModel;
import io.swagger.v3.oas.models.tags.Tag;

public final class EndpointNode extends NodeImpl<ClassInfoModel, Tag> {
    public EndpointNode(@Nonnull ClassInfoModel source,
        @Nonnull Tag target) {
        super(source, target);
    }
}
