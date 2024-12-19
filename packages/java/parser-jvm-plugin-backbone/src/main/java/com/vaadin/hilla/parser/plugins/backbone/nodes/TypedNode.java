package com.vaadin.hilla.parser.plugins.backbone.nodes;

import java.util.List;
import java.util.function.UnaryOperator;

import com.vaadin.hilla.parser.models.SignatureModel;
import io.swagger.v3.oas.models.media.Schema;
import org.jspecify.annotations.NonNull;

public interface TypedNode extends AnnotatedNode {
    @NonNull
    SignatureModel getType();

    Schema<?> getTarget();

    void setTarget(Schema<?> target);

    @NonNull
    TypedNode processType(@NonNull UnaryOperator<SignatureModel> typeProcessor);
}
