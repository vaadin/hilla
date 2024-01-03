package com.vaadin.hilla.parser.plugins.backbone.nodes;

import javax.annotation.Nonnull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.SignatureModel;

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
