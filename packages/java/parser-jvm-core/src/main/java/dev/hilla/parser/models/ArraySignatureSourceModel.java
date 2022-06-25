package dev.hilla.parser.models;

import java.util.List;

import io.github.classgraph.ArrayTypeSignature;

final class ArraySignatureSourceModel
        extends ArraySignatureAbstractModel<ArrayTypeSignature>
        implements SourceSignatureModel {

    public ArraySignatureSourceModel(ArrayTypeSignature origin) {
        super(origin);
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return AnnotationUtils.convert(origin.getTypeAnnotationInfo());
    }

    @Override
    protected SignatureModel prepareNestedType() {
        return SignatureModel.of(origin.getNestedType());
    }
}
