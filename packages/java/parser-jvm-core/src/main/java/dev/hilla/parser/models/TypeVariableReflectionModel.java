package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
            annotations = Arrays.stream(origin.getAnnotations())
                .map(annotation -> AnnotationInfoModel.of(annotation, this))
                .collect(Collectors.toList());
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
