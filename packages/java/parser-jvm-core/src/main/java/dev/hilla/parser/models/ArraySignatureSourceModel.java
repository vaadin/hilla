package dev.hilla.parser.models;

import io.github.classgraph.ArrayTypeSignature;

final class ArraySignatureSourceModel
        extends AbstractSourceSignatureModel<ArrayTypeSignature>
        implements ArraySignatureModel, SourceModel {
    private SignatureModel nestedType;

    public ArraySignatureSourceModel(ArrayTypeSignature origin, Model parent) {
        super(origin, parent);
    }

    @Override
    public SignatureModel getNestedType() {
        if (nestedType == null) {
            nestedType = SignatureModel.of(origin.getNestedType(), this);
        }

        return nestedType;
    }
}
