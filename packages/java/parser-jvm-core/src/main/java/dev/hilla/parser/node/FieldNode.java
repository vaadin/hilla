package dev.hilla.parser.node;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.FieldInfoModel;
import io.swagger.v3.oas.models.media.Schema;

public final class FieldNode extends NodeImpl<FieldInfoModel, Schema<?>> {
    public FieldNode(@Nonnull FieldInfoModel source,
        @Nonnull Schema<?> target) {
        super(source, target);
    }
}
