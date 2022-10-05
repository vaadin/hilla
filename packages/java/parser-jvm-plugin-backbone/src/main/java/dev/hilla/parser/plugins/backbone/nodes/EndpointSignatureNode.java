package dev.hilla.parser.plugins.backbone.nodes;

import javax.annotation.Nonnull;

import dev.hilla.parser.core.AbstractNode;
import dev.hilla.parser.models.SignatureModel;
import io.swagger.v3.oas.models.PathItem;

public final class EndpointSignatureNode
        extends AbstractNode<SignatureModel, Void> {
    private EndpointSignatureNode(@Nonnull SignatureModel source) {
        super(source, null);
    }

    @Nonnull
    static public EndpointSignatureNode of(@Nonnull SignatureModel source) {
        return new EndpointSignatureNode(source);
    }
}
