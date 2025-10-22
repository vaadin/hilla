package com.vaadin.hilla.typescript.parser.test.nodes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.typescript.parser.core.AbstractNode;
import com.vaadin.hilla.typescript.parser.models.ClassInfoModel;
import io.swagger.v3.oas.models.tags.Tag;

public final class EndpointNode extends AbstractNode<ClassInfoModel, Tag> {
    private EndpointNode(@NonNull ClassInfoModel value, @NonNull Tag schema) {
        super(value, schema);
    }

    @NonNull
    public static EndpointNode of(@NonNull ClassInfoModel cls) {
        return new EndpointNode(cls, new Tag());
    }
}
