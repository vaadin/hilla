package com.vaadin.hilla.parser.plugins.backbone.nodes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.ClassInfoModel;

public final class EndpointNonExposedNode
        extends AbstractNode<ClassInfoModel, Void> {
    private EndpointNonExposedNode(@NonNull ClassInfoModel classInfo) {
        super(classInfo, null);
    }

    @NonNull
    public static EndpointNonExposedNode of(@NonNull ClassInfoModel classInfo) {
        return new EndpointNonExposedNode(classInfo);
    }
}
