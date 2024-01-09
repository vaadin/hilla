package com.vaadin.hilla.parser.plugins.backbone.nodes;

import javax.annotation.Nonnull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.jackson.JacksonPropertyModel;

public final class PropertyNode
        extends AbstractNode<JacksonPropertyModel, String> {
    private PropertyNode(@Nonnull JacksonPropertyModel source,
            @Nonnull String target) {
        super(source, target);
    }

    @Nonnull
    static public PropertyNode of(@Nonnull JacksonPropertyModel source) {
        return new PropertyNode(source, "");
    }
}
