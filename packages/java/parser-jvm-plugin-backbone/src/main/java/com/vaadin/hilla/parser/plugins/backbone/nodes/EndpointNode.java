package com.vaadin.hilla.parser.plugins.backbone.nodes;

import javax.annotation.Nonnull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.ClassInfoModel;
import io.swagger.v3.oas.models.tags.Tag;

public final class EndpointNode extends AbstractNode<ClassInfoModel, Tag> {
    private EndpointNode(@Nonnull ClassInfoModel value, @Nonnull Tag schema) {
        super(value, schema);
    }

    @Nonnull
    public static EndpointNode of(@Nonnull ClassInfoModel cls) {
        return new EndpointNode(cls, new Tag());
    }
}
