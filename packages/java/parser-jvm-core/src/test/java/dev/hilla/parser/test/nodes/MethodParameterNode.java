package dev.hilla.parser.test.nodes;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AbstractNode;
import dev.hilla.parser.models.MethodParameterInfoModel;

public final class MethodParameterNode
        extends AbstractNode<MethodParameterInfoModel, String> {
    private MethodParameterNode(@Nonnull MethodParameterInfoModel source,
            String target) {
        super(source, target);
    }

    @Nonnull
    static public MethodParameterNode of(
            @Nonnull MethodParameterInfoModel source, @Nonnull String target) {
        return new MethodParameterNode(source, target);
    }
}
