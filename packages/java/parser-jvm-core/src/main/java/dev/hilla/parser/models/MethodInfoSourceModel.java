package dev.hilla.parser.models;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import io.github.classgraph.MethodInfo;

final class MethodInfoSourceModel extends AbstractModel<MethodInfo>
        implements MethodInfoModel, SourceModel {
    private Collection<MethodParameterInfoModel> parameters;
    private SignatureModel resultType;

    public MethodInfoSourceModel(MethodInfo method, Model parent) {
        super(method, parent);
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public Collection<MethodParameterInfoModel> getParameters() {
        if (parameters == null) {
            parameters = Arrays.stream(origin.getParameterInfo()).map(
                    parameter -> MethodParameterInfoModel.of(parameter, this))
                    .collect(Collectors.toSet());
        }

        return parameters;
    }

    @Override
    public SignatureModel getResultType() {
        if (resultType == null) {
            resultType = SignatureModel.of(
                    origin.getTypeSignatureOrTypeDescriptor().getResultType(),
                    this);
        }

        return resultType;
    }
}
