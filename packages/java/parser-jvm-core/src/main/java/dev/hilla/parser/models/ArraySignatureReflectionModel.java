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
    public SignatureModel getNestedType() {
        if (nestedType == null) {
            nestedType = SignatureModel
                    .of(origin.getAnnotatedGenericComponentType(), this);
        }

        return nestedType;
    }
}
