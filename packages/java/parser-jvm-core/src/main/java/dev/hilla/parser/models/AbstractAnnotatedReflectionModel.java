package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public abstract class AbstractAnnotatedReflectionModel<T extends AnnotatedElement>
        extends AbstractModel<T> implements AnnotatedModel, ReflectionModel {
    protected List<AnnotationInfoModel> annotations;

    AbstractAnnotatedReflectionModel(@Nonnull T origin) {
        super(origin);
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = Arrays.stream(origin.getAnnotations())
                    .map(AnnotationInfoModel::of).collect(Collectors.toList());
        }
        return annotations;
    }
}
