package dev.hilla.parser.node;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.MethodParameterInfoModel;
import io.swagger.v3.oas.models.media.Schema;

public final class MethodParameterNode extends NodeImpl<MethodParameterInfoModel, Schema<?>> {
    public MethodParameterNode(@Nonnull MethodParameterInfoModel source,
        Schema<?> target) {
        super(source, target);
    }
}
