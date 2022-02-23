package dev.hilla.parser.models;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class MethodParameterInfoReflectionModel extends AbstractModel<Parameter>
        implements MethodParameterInfoModel, ReflectionModel {
    private List<AnnotationInfoModel> annotations;
    private SignatureModel type;

    public MethodParameterInfoReflectionModel(Parameter parameter,
            Model parent) {
        super(parameter, parent);
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
    public String getName() {
        return origin.getName();
    }

    @Override
    public SignatureModel getType() {
        if (type == null) {
            type = SignatureModel.of(origin.getAnnotatedType(), this);
        }

        return type;
    }
}
