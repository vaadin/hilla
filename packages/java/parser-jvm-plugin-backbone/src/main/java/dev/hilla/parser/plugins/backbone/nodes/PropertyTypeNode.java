package dev.hilla.parser.plugins.backbone.nodes;

import dev.hilla.parser.core.AbstractNode;
import dev.hilla.parser.models.jackson.JacksonPropertyTypeModel;

import io.swagger.v3.oas.models.media.Schema;
import jakarta.annotation.Nonnull;

public class PropertyTypeNode
        extends AbstractNode<JacksonPropertyTypeModel, Schema<?>> {
    private PropertyTypeNode(JacksonPropertyTypeModel source,
            Schema<?> target) {
        super(source, target);
    }

    @Nonnull
    static public PropertyTypeNode of(
            @Nonnull JacksonPropertyTypeModel source) {
        return new PropertyTypeNode(source, new Schema<>());
    }
}
