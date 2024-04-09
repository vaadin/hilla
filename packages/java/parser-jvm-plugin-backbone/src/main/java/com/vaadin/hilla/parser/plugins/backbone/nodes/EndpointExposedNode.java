package com.vaadin.hilla.parser.plugins.backbone.nodes;

import javax.annotation.Nonnull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.ClassInfoModel;

public final class EndpointExposedNode
        extends AbstractNode<ClassInfoModel, Void> {
    private EndpointExposedNode(@Nonnull ClassInfoModel classInfo) {
        super(classInfo, null);
    }

    @Nonnull
    public static EndpointExposedNode of(@Nonnull ClassInfoModel classInfo) {
        return new EndpointExposedNode(classInfo);
    }
}
