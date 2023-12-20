package com.vaadin.hilla.parser.plugins.backbone.nodes;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.vaadin.hilla.parser.core.AbstractNode;
import com.vaadin.hilla.parser.models.AnnotatedModel;
import com.vaadin.hilla.parser.models.AnnotationInfoModel;
import com.vaadin.hilla.parser.models.SignatureModel;
import io.swagger.v3.oas.models.media.Schema;

public final class CompositeTypeSignatureNode extends
        AbstractNode<List<SignatureModel>, Schema<?>> implements TypedNode {

    private final List<AnnotationInfoModel> annotations;

    private CompositeTypeSignatureNode(@Nonnull List<SignatureModel> source,
            @Nonnull Schema<?> target, List<AnnotationInfoModel> annotations) {
        super(source, target);
        this.annotations = annotations;
    }

    private CompositeTypeSignatureNode(@Nonnull List<SignatureModel> source,
            @Nonnull Schema<?> target) {
        this(source, target, extractAnnotations(source));
    }

    @Nonnull
    static public CompositeTypeSignatureNode of(
            @Nonnull List<SignatureModel> source) {
        return new CompositeTypeSignatureNode(source, new Schema<>());
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        return annotations;
    }

    @Override
    public SignatureModel getType() {
        return getSource().get(0);
    }

    @Override
    public TypedNode processType(
            @Nonnull UnaryOperator<SignatureModel> typeProcessor) {
        var processedTypes = getSource().stream().map(typeProcessor)
                .collect(Collectors.toList());
        if (processedTypes.equals(getSource())) {
            return this;
        }

        return new CompositeTypeSignatureNode(processedTypes, getTarget(),
                annotations);
    }

    private static List<AnnotationInfoModel> extractAnnotations(
            List<SignatureModel> source) {
        return source.stream().map(AnnotatedModel::getAnnotations)
                .flatMap(Collection::stream).distinct()
                .collect(Collectors.toList());
    }
}
