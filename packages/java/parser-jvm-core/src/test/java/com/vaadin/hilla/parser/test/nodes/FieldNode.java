package com.vaadin.hilla.parser.test.nodes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.FieldInfoModel;

public final class FieldNode extends AbstractNode<FieldInfoModel, String> {
    private FieldNode(@NonNull FieldInfoModel source, @NonNull String target) {
        super(source, target);
    }

    @NonNull
    static public FieldNode of(@NonNull FieldInfoModel source) {
        return new FieldNode(source, "");
    }
}
