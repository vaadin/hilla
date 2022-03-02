package dev.hilla.parser.models;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.AnnotationInfo;

public abstract class AbstractAnnotatedSourceModel<T> extends AbstractModel<T>
        implements AnnotatedModel, SourceModel {
    protected List<AnnotationInfoModel> annotations;

    AbstractAnnotatedSourceModel(@Nonnull T origin, Model parent) {
        super(origin, parent);
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = getOriginAnnotations().map(this::processAnnotation)
                    .collect(Collectors.toList());
        }

        return annotations;
    }

    protected abstract Stream<AnnotationInfo> getOriginAnnotations();

    private AnnotationInfoModel processAnnotation(AnnotationInfo annotation) {
        return AnnotationInfoModel.of(annotation, this);
    }
}
