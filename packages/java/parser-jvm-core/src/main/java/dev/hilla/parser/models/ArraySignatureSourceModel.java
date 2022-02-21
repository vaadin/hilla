package dev.hilla.parser.models;

import java.util.List;

import io.github.classgraph.ArrayTypeSignature;

final class ArraySignatureSourceModel extends AbstractModel<ArrayTypeSignature>
        implements ArraySignatureModel, SourceSignatureModel {
    private List<AnnotationInfoModel> annotations;
    private SignatureModel nestedType;

    public ArraySignatureSourceModel(ArrayTypeSignature origin, Model parent) {
        super(origin, parent);
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = AnnotationUtils.processTypeAnnotations(origin.getTypeAnnotationInfo(), this);
        }

        return annotations;
    }

    @Override
    public SignatureModel getNestedType() {
        if (nestedType == null) {
            nestedType = SignatureModel.of(origin.getNestedType(), this);
        }

        return nestedType;
    }
}
