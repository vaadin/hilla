package dev.hilla.parser.node;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.MethodInfoModel;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;

public final class MethodNode extends NodeImpl<MethodInfoModel, PathItem> {
    public MethodNode(@Nonnull MethodInfoModel source,
        @Nonnull PathItem target) {
        super(source, target);
    }
}
