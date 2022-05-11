package dev.hilla.parser.models;

import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.TypeVariableSignature;

final class TypeVariableSourceModel
        extends AbstractAnnotatedSourceModel<TypeVariableSignature>
        implements TypeVariableModel, SourceSignatureModel {
    private TypeParameterModel typeParameter;

    public TypeVariableSourceModel(TypeVariableSignature origin, Model parent) {
        super(origin, parent);
    }

    public TypeParameterModel resolve() {
        if (typeParameter == null) {
            typeParameter = TypeParameterModel.of(origin.resolve(), this);
        }

        return typeParameter;
    }

    @Override
    protected Stream<AnnotationInfo> getOriginAnnotations() {
        return origin.getTypeAnnotationInfo().stream();
    }
}
