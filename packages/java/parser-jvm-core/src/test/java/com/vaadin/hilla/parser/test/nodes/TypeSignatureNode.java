package com.vaadin.hilla.parser.test.nodes;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.SignatureModel;
import io.swagger.v3.oas.models.media.Schema;

public final class TypeSignatureNode
        extends AbstractNode<SignatureModel, Schema<?>> {
    private TypeSignatureNode(@NonNull SignatureModel source,
            @NonNull Schema<?> target) {
        super(source, target);
    }

    @NonNull
    static public TypeSignatureNode of(@NonNull SignatureModel source) {
        return new TypeSignatureNode(source, new Schema<>());
    }
}
