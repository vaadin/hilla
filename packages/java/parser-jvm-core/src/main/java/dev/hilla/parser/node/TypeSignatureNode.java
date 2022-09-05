package dev.hilla.parser.node;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.SignatureModel;
import io.swagger.v3.oas.models.media.Schema;

public final class TypeSignatureNode extends NodeImpl<SignatureModel, Schema<?>> {
    public TypeSignatureNode(@Nonnull SignatureModel value,
        @Nonnull Schema<?> schema) {
        super(value, schema);
    }
}
