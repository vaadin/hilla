package com.vaadin.hilla.parser.plugins.backbone.nodes;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.AnnotationInfoModel;
import com.vaadin.hilla.parser.models.SignatureModel;

import io.swagger.v3.oas.models.media.Schema;
import org.jspecify.annotations.NonNull;

public final class TypeSignatureNode
        extends AbstractNode<SignatureModel, Schema<?>> implements TypedNode {
    private final List<AnnotationInfoModel> annotations;
    private final Integer position;

    private TypeSignatureNode(SignatureModel source, Schema<?> target,
            List<AnnotationInfoModel> annotations, Integer position) {
        super(source, target);
        this.annotations = annotations;
        this.position = position;
    }

    private TypeSignatureNode(SignatureModel source, Schema<?> target,
            Integer position) {
        this(source, target, source.getAnnotations(), position);
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
                getTarget(), annotations, position);
    }

    @NonNull
    static public TypeSignatureNode of(@NonNull SignatureModel source) {
        return new TypeSignatureNode(source, new Schema<>(), null);
    }

    @NonNull
    static public TypeSignatureNode of(@NonNull SignatureModel source,
            int position) {
        return new TypeSignatureNode(source, new Schema<>(), position);
    }

    @Override
    public boolean equals(Object o) {
        boolean eq = super.equals(o);

        if (eq) {
            var other = (TypeSignatureNode) o;
            eq = Objects.equals(position, other.position);
        }

        return eq;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Objects.hashCode(position);
    }

    @Override
    public String toString() {
        var str = super.toString();

        if (position != null) {
            str += "[" + position + "]";
        }

        return str;
    }
}
