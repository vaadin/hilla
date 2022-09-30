package dev.hilla.parser.models;

import javax.annotation.Nonnull;
import java.lang.reflect.AnnotatedTypeVariable;
import java.util.List;
import java.util.Objects;

import io.github.classgraph.TypeVariableSignature;

/**
 * A synthetic pre-resolved TypeVariableModel implementation.
 *
 * @deprecated
 */
@Deprecated
final class TypeVariableSyntheticModel extends TypeVariableModel {
    private final TypeParameterModel typeParameter;
    private final List<AnnotationInfoModel> annotations;

    TypeVariableSyntheticModel(@Nonnull TypeParameterModel typeParameter,
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
