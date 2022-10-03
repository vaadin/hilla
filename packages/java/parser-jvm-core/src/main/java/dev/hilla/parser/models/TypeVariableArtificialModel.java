package dev.hilla.parser.models;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A synthetic pre-resolved TypeVariableModel implementation.
 */
final class TypeVariableArtificialModel extends TypeVariableModel {
    private final TypeParameterModel typeParameter;
    private final List<AnnotationInfoModel> annotations;

    TypeVariableArtificialModel(@Nonnull TypeParameterModel typeParameter,
            @Nonnull List<AnnotationInfoModel> annotations) {
        this.typeParameter = typeParameter;
        this.annotations = annotations;
    }

    @Override
    protected TypeParameterModel prepareResolved() {
        return typeParameter;
    }

    @Override
    public List<AnnotationInfoModel> prepareAnnotations() {
        return annotations;
    }

    @Override
    public Object get() {
        return null;
    }

    @Override
    public String getName() {
        return typeParameter.getName();
    }
}
