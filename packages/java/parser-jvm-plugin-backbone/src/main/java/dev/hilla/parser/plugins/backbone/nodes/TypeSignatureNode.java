package dev.hilla.parser.plugins.backbone.nodes;

import java.util.List;
import java.util.function.UnaryOperator;

import dev.hilla.parser.core.AbstractNode;
import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.SignatureModel;

import io.swagger.v3.oas.models.media.Schema;
import jakarta.annotation.Nonnull;

public final class TypeSignatureNode
        extends AbstractNode<SignatureModel, Schema<?>> implements TypedNode {
    private final List<AnnotationInfoModel> annotations;

    private TypeSignatureNode(SignatureModel source, Schema<?> target,
            List<AnnotationInfoModel> annotations) {
        super(source, target);
        this.annotations = annotations;
    }

    private TypeSignatureNode(SignatureModel source, Schema<?> target) {
        this(source, target, source.getAnnotations());
    }

    public List<AnnotationInfoModel> getAnnotations() {
        return annotations;
    }

    public SignatureModel getType() {
        return getSource();
    }

    @Override
    public TypedNode processType(UnaryOperator<SignatureModel> typeProcessor) {
        var processedType = typeProcessor.apply(getSource());
        if (processedType.equals(getSource())) {
            return this;
        }

        return new TypeSignatureNode(typeProcessor.apply(getSource()),
                getTarget(), annotations);
    }

    @Nonnull
    static public TypeSignatureNode of(@Nonnull SignatureModel source) {
        return new TypeSignatureNode(source, new Schema<>());
    }
}
