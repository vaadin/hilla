package dev.hilla.parser.models;

import java.lang.reflect.Parameter;
import java.util.Objects;

final class MethodParameterInfoReflectionModel
        extends AbstractAnnotatedReflectionModel<Parameter>
        implements MethodParameterInfoModel, ReflectionModel {
    private SignatureModel type;

    public MethodParameterInfoReflectionModel(Parameter parameter,
            Model parent) {
        super(parameter, parent);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof MethodParameterInfoModel)) {
            return false;
        }

        if (other instanceof MethodParameterInfoReflectionModel) {
            return Objects.equals(origin,
                    ((MethodParameterInfoReflectionModel) other).origin);
        }

        return Objects.equals(getName(),
                ((MethodParameterInfoModel) other).getName());
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
