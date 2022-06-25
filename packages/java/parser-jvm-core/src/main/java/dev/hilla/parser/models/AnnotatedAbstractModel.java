package dev.hilla.parser.models;

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

abstract class AnnotatedAbstractModel<T> extends AbstractModel<T>
        implements AnnotatedModel {
    private List<AnnotationInfoModel> annotations;

    AnnotatedAbstractModel(@Nonnull T origin) {
        super(origin);
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = prepareAnnotations();
        }

        return annotations;
    }

    @Override
    public Stream<AnnotationInfoModel> getAnnotationsStream() {
        return getAnnotations().stream();
    }

    protected abstract List<AnnotationInfoModel> prepareAnnotations();
}
