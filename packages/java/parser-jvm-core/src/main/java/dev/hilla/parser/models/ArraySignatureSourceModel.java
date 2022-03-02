package dev.hilla.parser.models;

import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ArrayTypeSignature;

final class ArraySignatureSourceModel
        extends AbstractAnnotatedSourceModel<ArrayTypeSignature>
        implements ArraySignatureModel, SourceSignatureModel {
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

    @Override
    protected Stream<AnnotationInfo> getOriginAnnotations() {
        return origin.getTypeAnnotationInfo().stream();
    }
}
