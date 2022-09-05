package dev.hilla.parser.node;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.ClassInfoModel;
import io.swagger.v3.oas.models.media.Schema;

public final class EntityNode extends NodeImpl<ClassInfoModel, Schema<?>> {
    public EntityNode(@Nonnull ClassInfoModel source,
        @Nonnull Schema<?> target) {
        super(source, target);
    }
}
