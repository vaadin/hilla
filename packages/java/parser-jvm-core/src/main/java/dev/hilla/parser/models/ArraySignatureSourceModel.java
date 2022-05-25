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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ArraySignatureModel)) {
            return false;
        }

        var other = (ArraySignatureModel) obj;

        return getNestedType().equals(other.getNestedType())
                && getAnnotations().equals(other.getAnnotations());
    }

    @Override
    public SignatureModel getNestedType() {
        if (nestedType == null) {
            nestedType = SignatureModel.of(origin.getNestedType());
        }

        return nestedType;
    }

    @Override
    public int hashCode() {
        return 1 + getNestedType().hashCode();
    }

    @Override
    protected Stream<AnnotationInfo> getOriginAnnotations() {
        var annotations = origin.getTypeAnnotationInfo();
        return annotations != null ? annotations.stream() : Stream.empty();
    }
}
