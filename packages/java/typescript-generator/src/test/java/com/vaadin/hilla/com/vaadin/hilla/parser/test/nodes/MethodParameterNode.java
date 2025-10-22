package com.vaadin.hilla.typescript.parser.test.nodes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.typescript.parser.core.AbstractNode;
import com.vaadin.hilla.typescript.parser.models.MethodParameterInfoModel;

public final class MethodParameterNode
        extends AbstractNode<MethodParameterInfoModel, String> {
    private MethodParameterNode(@NonNull MethodParameterInfoModel source,
            String target) {
        super(source, target);
    }

    @NonNull
    static public MethodParameterNode of(
            @NonNull MethodParameterInfoModel source, @NonNull String target) {
        return new MethodParameterNode(source, target);
    }
}
