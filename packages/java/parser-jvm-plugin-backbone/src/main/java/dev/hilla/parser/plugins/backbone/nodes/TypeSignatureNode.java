package dev.hilla.parser.plugins.backbone.nodes;

import jakarta.annotation.Nonnull;

import dev.hilla.parser.core.AbstractNode;
import dev.hilla.parser.models.SignatureModel;
import io.swagger.v3.oas.models.media.Schema;

public final class TypeSignatureNode
        extends AbstractNode<SignatureModel, Schema<?>> {
    private TypeSignatureNode(@Nonnull SignatureModel source,
            @Nonnull Schema<?> target) {
        super(source, target);
    }

    @Nonnull
    static public TypeSignatureNode of(@Nonnull SignatureModel source) {
        return new TypeSignatureNode(source, new Schema<>());
    }
}
