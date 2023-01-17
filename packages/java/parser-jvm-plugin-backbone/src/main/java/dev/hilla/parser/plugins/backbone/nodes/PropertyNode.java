package dev.hilla.parser.plugins.backbone.nodes;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AbstractNode;
import dev.hilla.parser.models.PropertyInfoModel;

public final class PropertyNode
        extends AbstractNode<PropertyInfoModel, String> {
    private PropertyNode(@Nonnull PropertyInfoModel source,
            @Nonnull String target) {
        super(source, target);
    }

    @Nonnull
    static public PropertyNode of(@Nonnull PropertyInfoModel source) {
        return new PropertyNode(source, "");
    }
}
