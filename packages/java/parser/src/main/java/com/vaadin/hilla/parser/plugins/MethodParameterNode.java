package com.vaadin.hilla.parser.plugins.backbone.nodes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.MethodParameterInfoModel;

public class MethodParameterNode
        extends AbstractNode<MethodParameterInfoModel, String> {
    protected MethodParameterNode(@NonNull MethodParameterInfoModel source,
            String target) {
        super(source, target);
    }

    @NonNull
    static public MethodParameterNode of(
            @NonNull MethodParameterInfoModel source, @NonNull String target) {
        return new MethodParameterNode(source, target);
    }
}
