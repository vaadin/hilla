package com.vaadin.hilla.parser.plugins.backbone.nodes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.ClassInfoModel;

public class EndpointExposedNode
        extends AbstractNode<ClassInfoModel, Void> {
    protected EndpointExposedNode(@NonNull ClassInfoModel classInfo) {
        super(classInfo, null);
    }

    @NonNull
    public static EndpointExposedNode of(@NonNull ClassInfoModel classInfo) {
        return new EndpointExposedNode(classInfo);
    }
}
