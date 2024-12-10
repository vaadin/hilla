package com.vaadin.hilla.parser.plugins.backbone.nodes;

import javax.annotation.Nonnull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.MethodInfoModel;
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
