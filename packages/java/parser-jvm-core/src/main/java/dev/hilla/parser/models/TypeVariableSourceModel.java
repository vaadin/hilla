package dev.hilla.parser.models;

import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.TypeVariableSignature;

final class TypeVariableSourceModel
        extends AbstractAnnotatedSourceModel<TypeVariableSignature>
        implements TypeVariableModel, SourceSignatureModel {
    private TypeParameterModel typeParameter;

    public TypeVariableSourceModel(TypeVariableSignature origin) {
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

        return origin.getName().equals(other.getName())
                && getAnnotations().equals(other.getAnnotations());
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public int hashCode() {
        return origin.getName().hashCode();
    }

    @Override
    public TypeParameterModel resolve() {
        if (typeParameter == null) {
            typeParameter = TypeParameterModel.of(origin.resolve());
        }

        return typeParameter;
    }

    @Override
    protected Stream<AnnotationInfo> getOriginAnnotations() {
        var annotations = origin.getTypeAnnotationInfo();
        return annotations != null ? annotations.stream() : Stream.empty();
    }
}
