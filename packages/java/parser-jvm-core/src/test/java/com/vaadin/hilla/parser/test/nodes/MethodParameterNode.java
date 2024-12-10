package com.vaadin.hilla.parser.test.nodes;

import javax.annotation.Nonnull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.MethodParameterInfoModel;

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
