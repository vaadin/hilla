package dev.hilla.parser.models;

import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ArrayTypeSignature;

final class ArraySignatureSourceModel
        extends AbstractAnnotatedSourceModel<ArrayTypeSignature>
        implements ArraySignatureModel, SourceSignatureModel {
    private SignatureModel nestedType;

    public ArraySignatureSourceModel(ArrayTypeSignature origin) {
        super(origin);
    }

    @Override
    public SignatureModel getNestedType() {
        if (nestedType == null) {
            nestedType = SignatureModel.of(origin.getNestedType());
        }

        return nestedType;
    }

    @Override
    protected Stream<AnnotationInfo> getOriginAnnotations() {
        var annotations = origin.getTypeAnnotationInfo();
        return annotations != null ? annotations.stream() : Stream.empty();
    }
}
