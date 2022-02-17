package dev.hilla.parser.models;

import io.github.classgraph.FieldInfo;

final class FieldInfoSourceModel extends AbstractModel<FieldInfo>
        implements FieldInfoModel, SourceModel {
    private SignatureModel type;

    public FieldInfoSourceModel(FieldInfo field, Model parent) {
        super(field, parent);
    }

    @Override
    public String getName() {
        return origin.getName();
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
