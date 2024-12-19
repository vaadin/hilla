package com.vaadin.hilla.parser.plugins.backbone.nodes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.jackson.JacksonPropertyModel;

public final class PropertyNode
        extends AbstractNode<JacksonPropertyModel, String> {
    private PropertyNode(@NonNull JacksonPropertyModel source,
            @NonNull String target) {
        super(source, target);
    }

    @NonNull
    static public PropertyNode of(@NonNull JacksonPropertyModel source) {
        return new PropertyNode(source, "");
    }
}
