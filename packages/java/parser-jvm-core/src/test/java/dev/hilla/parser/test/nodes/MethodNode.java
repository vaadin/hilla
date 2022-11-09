package dev.hilla.parser.test.nodes;

import jakarta.annotation.Nonnull;

import dev.hilla.parser.core.AbstractNode;
import dev.hilla.parser.models.MethodInfoModel;
import io.swagger.v3.oas.models.PathItem;

public final class MethodNode extends AbstractNode<MethodInfoModel, PathItem> {
    private MethodNode(@Nonnull MethodInfoModel source,
            @Nonnull PathItem target) {
        super(source, target);
    }

    @Nonnull
    static public MethodNode of(@Nonnull MethodInfoModel model) {
        return new MethodNode(model, new PathItem());
    }
}
