package com.vaadin.hilla.parser.plugins.backbone.nodes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.MethodInfoModel;
import io.swagger.v3.oas.models.PathItem;

public class MethodNode extends AbstractNode<MethodInfoModel, PathItem> {
    protected MethodNode(@NonNull MethodInfoModel source,
            @NonNull PathItem target) {
        super(source, target);
    }

    @NonNull
    static public MethodNode of(@NonNull MethodInfoModel model) {
        return new MethodNode(model, new PathItem());
    }
}
