package dev.hilla.parser.models;

import java.lang.reflect.Parameter;

final class MethodParameterInfoReflectionModel extends AbstractModel<Parameter>
        implements MethodParameterInfoModel, ReflectionModel {
    private SignatureModel type;

    public MethodParameterInfoReflectionModel(Parameter parameter,
            Model parent) {
        super(parameter, parent);
    }

    @Override
    public SignatureModel getType() {
        if (type == null) {
            type = SignatureModel.of(origin.getAnnotatedType(), this);
        }

        return type;
    }
}
