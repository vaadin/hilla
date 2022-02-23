package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;
import java.util.List;

final class TypeVariableReflectionModel
        extends AbstractModel<AnnotatedTypeVariable>
        implements TypeVariableModel, ReflectionSignatureModel {
    private List<AnnotationInfoModel> annotations;
    private TypeParameterModel typeParameter;

    public TypeVariableReflectionModel(AnnotatedTypeVariable origin,
            Model parent) {
        super(origin, parent);
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = new AnnotationProcessor.Reflection(this).add(origin)
                    .process();
        }

        return annotations;
    }

    @Override
    public SignatureModel resolve() {
        if (typeParameter == null) {
            typeParameter = TypeParameterModel.of(origin, parent);
        }

        return typeParameter;
    }
}
