package dev.hilla.parser.models;

import java.util.List;

import io.github.classgraph.TypeVariableSignature;

final class TypeVariableSourceModel extends AbstractModel<TypeVariableSignature>
        implements TypeVariableModel, SourceSignatureModel {
    private List<AnnotationInfoModel> annotations;
    private TypeParameterModel typeParameter;

    public TypeVariableSourceModel(TypeVariableSignature origin, Model parent) {
        super(origin, parent);
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = new AnnotationProcessor.Source(this).add(origin)
                    .process();
        }

        return annotations;
    }

    public TypeParameterModel resolve() {
        if (typeParameter == null) {
            typeParameter = TypeParameterModel.of(origin.resolve(), this);
        }

        return typeParameter;
    }
}
