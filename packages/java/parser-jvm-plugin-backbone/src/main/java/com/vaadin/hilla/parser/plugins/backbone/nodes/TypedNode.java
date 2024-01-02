package com.vaadin.hilla.parser.plugins.backbone.nodes;

import java.util.List;
import java.util.function.UnaryOperator;

import com.vaadin.hilla.parser.models.SignatureModel;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.annotation.Nonnull;

public interface TypedNode extends AnnotatedNode {
    @Nonnull
    SignatureModel getType();

    Schema<?> getTarget();

    void setTarget(Schema<?> target);

    @Nonnull
    TypedNode processType(@Nonnull UnaryOperator<SignatureModel> typeProcessor);
}
