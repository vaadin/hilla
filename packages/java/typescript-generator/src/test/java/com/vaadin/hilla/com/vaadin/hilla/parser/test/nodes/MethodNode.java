package com.vaadin.hilla.typescript.parser.test.nodes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.typescript.parser.core.AbstractNode;
import com.vaadin.hilla.typescript.parser.models.MethodInfoModel;
import io.swagger.v3.oas.models.PathItem;

public final class MethodNode extends AbstractNode<MethodInfoModel, PathItem> {
    private MethodNode(@NonNull MethodInfoModel source,
            @NonNull PathItem target) {
        super(source, target);
    }

    @NonNull
    static public MethodNode of(@NonNull MethodInfoModel model) {
        return new MethodNode(model, new PathItem());
    }
}
