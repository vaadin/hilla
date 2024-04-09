package com.vaadin.hilla.parser.test.nodes;

import javax.annotation.Nonnull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.FieldInfoModel;

public final class FieldNode extends AbstractNode<FieldInfoModel, String> {
    private FieldNode(@Nonnull FieldInfoModel source, @Nonnull String target) {
        super(source, target);
    }

    @Nonnull
    static public FieldNode of(@Nonnull FieldInfoModel source) {
        return new FieldNode(source, "");
    }
}
