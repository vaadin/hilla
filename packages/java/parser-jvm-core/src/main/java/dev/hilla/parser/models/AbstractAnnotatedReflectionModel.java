package dev.hilla.parser.models;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public abstract class AbstractAnnotatedReflectionModel<T extends AnnotatedElement>
        extends AbstractModel<T> implements AnnotatedModel, ReflectionModel {
    protected List<AnnotationInfoModel> annotations;

    AbstractAnnotatedReflectionModel(@Nonnull T origin, Model parent) {
        super(origin, parent);
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = Arrays.stream(origin.getAnnotations())
                    .map(this::processAnnotation).collect(Collectors.toList());
        }
        return annotations;
    }

    private AnnotationInfoModel processAnnotation(Annotation annotation) {
        return AnnotationInfoModel.of(annotation, this);
    }
}
