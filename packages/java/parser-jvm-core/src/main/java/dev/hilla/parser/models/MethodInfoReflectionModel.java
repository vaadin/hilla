package dev.hilla.parser.models;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

final class MethodInfoReflectionModel extends AbstractModel<Method>
        implements MethodInfoModel, ReflectionModel {
    private Collection<MethodParameterInfoModel> parameters;
    private SignatureModel resultType;

    public MethodInfoReflectionModel(Method method, Model parent) {
        super(method, parent);
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public Collection<MethodParameterInfoModel> getParameters() {
        if (parameters == null) {
            parameters = Arrays.stream(origin.getParameters()).map(
                    parameter -> MethodParameterInfoModel.of(parameter, this))
                    .collect(Collectors.toSet());
        }

        return parameters;
    }

    @Override
    public SignatureModel getResultType() {
        if (resultType == null) {
            resultType = SignatureModel.of(origin.getAnnotatedReturnType(),
                    this);
        }

        return resultType;
    }
}
