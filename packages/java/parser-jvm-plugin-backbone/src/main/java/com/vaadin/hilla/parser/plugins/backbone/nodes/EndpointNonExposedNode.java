package com.vaadin.hilla.parser.plugins.backbone.nodes;

import javax.annotation.Nonnull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.ClassInfoModel;

public final class EndpointNonExposedNode
        extends AbstractNode<ClassInfoModel, Void> {
    private EndpointNonExposedNode(@Nonnull ClassInfoModel classInfo) {
        super(classInfo, null);
    }

    @Nonnull
    public static EndpointNonExposedNode of(@Nonnull ClassInfoModel classInfo) {
        return new EndpointNonExposedNode(classInfo);
    }
}
