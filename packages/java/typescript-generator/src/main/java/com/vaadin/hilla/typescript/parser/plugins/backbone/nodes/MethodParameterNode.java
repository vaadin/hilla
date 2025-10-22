package com.vaadin.hilla.typescript.parser.plugins.backbone.nodes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.typescript.parser.core.AbstractNode;
import com.vaadin.hilla.typescript.parser.models.MethodParameterInfoModel;

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
