package dev.hilla.parser.node;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.MethodParameterInfoModel;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public final class MethodParameterNode extends
        AbstractNode<MethodParameterInfoModel, String> {
    private MethodParameterNode(@Nonnull MethodParameterInfoModel source,
        String target) {
        super(source, target);
    }

    static public MethodParameterNode of(@Nonnull MethodParameterInfoModel source) {
        return new MethodParameterNode(source, source.getName());
    }
}
