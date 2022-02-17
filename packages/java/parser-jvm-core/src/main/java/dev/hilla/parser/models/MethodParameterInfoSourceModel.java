package dev.hilla.parser.models;

import io.github.classgraph.MethodParameterInfo;

final class MethodParameterInfoSourceModel
        extends AbstractModel<MethodParameterInfo>
        implements MethodParameterInfoModel, SourceModel {
    private SignatureModel type;

    public MethodParameterInfoSourceModel(MethodParameterInfo parameter,
            Model parent) {
        super(parameter, parent);
    }

    @Override
    public SignatureModel getType() {
        if (type == null) {
            type = SignatureModel.of(origin.getTypeSignatureOrTypeDescriptor(),
                    this);
        }

        return type;
    }
}
