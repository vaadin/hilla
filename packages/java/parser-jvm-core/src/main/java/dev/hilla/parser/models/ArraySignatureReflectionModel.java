package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedArrayType;

final class ArraySignatureReflectionModel
        extends AbstractAnnotatedReflectionModel<AnnotatedArrayType>
        implements ArraySignatureModel, ReflectionSignatureModel {
    private SignatureModel nestedType;

    public ArraySignatureReflectionModel(AnnotatedArrayType origin) {
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
            nestedType = SignatureModel
                    .of(origin.getAnnotatedGenericComponentType());
        }

        return nestedType;
    }

    @Override
    public int hashCode() {
        return 1 + getNestedType().hashCode();
    }
}
