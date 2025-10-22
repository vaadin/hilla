package com.vaadin.hilla.parser.plugins.backbone.nodes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.SignatureModel;

public final class EndpointSignatureNode
        extends AbstractNode<SignatureModel, Void> {
    private EndpointSignatureNode(@NonNull SignatureModel source) {
        super(source, null);
    }

    @NonNull
    static public EndpointSignatureNode of(@NonNull SignatureModel source) {
        return new EndpointSignatureNode(source);
    }
}
