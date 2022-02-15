package dev.hilla.parser.models;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

final class ArraySignatureReflectionModel
        extends AbstractReflectionSignatureModel<Type>
        implements ArraySignatureModel, ReflectionModel {
    static final String ILLEGAL_ARGUMENTS_EXCEPTION_MSG = "ArraySignatureReflectionModel accepts only Class<?> and GenericArrayType as an origin type";

    private SignatureModel nestedType;

    public ArraySignatureReflectionModel(Type origin, Model parent) {
        super(origin, parent);
    }

    @Override
    public SignatureModel getNestedType() {
        if (nestedType == null) {
            nestedType = SignatureModel
                    .of(origin instanceof GenericArrayType
                            ? ((GenericArrayType) origin)
                                    .getGenericComponentType()
                            : ((Class<?>) origin).getComponentType(), this);
        }

        return nestedType;
    }
}
