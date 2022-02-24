package dev.hilla.parser.models;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class MethodParameterInfoReflectionModel extends AbstractAnnotatedReflectionModel<Parameter>
        implements MethodParameterInfoModel, ReflectionModel {
    private SignatureModel type;

    public MethodParameterInfoReflectionModel(Parameter parameter,
            Model parent) {
        super(parameter, parent);
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
