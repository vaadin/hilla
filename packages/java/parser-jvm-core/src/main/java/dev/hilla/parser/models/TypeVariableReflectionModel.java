package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.TypeVariable;

final class TypeVariableReflectionModel
        extends AbstractAnnotatedReflectionModel<AnnotatedTypeVariable>
        implements TypeVariableModel, ReflectionSignatureModel {
    private TypeParameterModel typeParameter;

    public TypeVariableReflectionModel(AnnotatedTypeVariable origin) {
        super(origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TypeVariableModel)) {
            return false;
        }

        var other = (TypeVariableModel) obj;

        return getName().equals(other.getName())
                && getAnnotations().equals(other.getAnnotations());
    }

    @Override
    public String getName() {
        return ((TypeVariable<?>) origin.getType()).getName();
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public SignatureModel resolve() {
        if (typeParameter == null) {
            typeParameter = TypeParameterModel.of(origin);
        }

        return typeParameter;
    }
}
