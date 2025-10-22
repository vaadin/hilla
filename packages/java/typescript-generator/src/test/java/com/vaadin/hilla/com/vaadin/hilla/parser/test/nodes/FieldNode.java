package com.vaadin.hilla.typescript.parser.test.nodes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.typescript.parser.core.AbstractNode;
import com.vaadin.hilla.typescript.parser.models.FieldInfoModel;

public final class FieldNode extends AbstractNode<FieldInfoModel, String> {
    private FieldNode(@NonNull FieldInfoModel source, @NonNull String target) {
        super(source, target);
    }

    @NonNull
    static public FieldNode of(@NonNull FieldInfoModel source) {
        return new FieldNode(source, "");
    }
}
