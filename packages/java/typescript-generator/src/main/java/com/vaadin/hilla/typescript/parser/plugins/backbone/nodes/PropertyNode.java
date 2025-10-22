package com.vaadin.hilla.typescript.parser.plugins.backbone.nodes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.typescript.parser.core.AbstractNode;
import com.vaadin.hilla.typescript.parser.models.jackson.JacksonPropertyModel;

public class PropertyNode extends AbstractNode<JacksonPropertyModel, String> {
    protected PropertyNode(@NonNull JacksonPropertyModel source,
            @NonNull String target) {
        super(source, target);
    }

    @NonNull
    static public PropertyNode of(@NonNull JacksonPropertyModel source) {
        return new PropertyNode(source, "");
    }
}
