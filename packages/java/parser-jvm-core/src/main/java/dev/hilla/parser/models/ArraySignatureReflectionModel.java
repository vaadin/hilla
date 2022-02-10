package dev.hilla.parser.models;

import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

final class ArraySignatureReflectionModel
        extends AbstractReflectionSignatureDependable<Class<?>, Dependable<?, ?>>
        implements ArraySignatureModel, ReflectionSignatureModel {
    private TypeModel nestedType;

    public ArraySignatureReflectionModel(Class<?> origin, Dependable<?, ?> parent) {
        super(origin, parent);
    }

    public static Stream<Class<?>> resolve(@Nonnull Class<?> signature) {
        if (!Objects.requireNonNull(signature).isArray()) {
            throw new IllegalArgumentException(
                    "Signature for ArraySignatureReflectionModel should be an array");
        }

        return ReflectionSignatureModel.resolve(signature);
    }

    @Override
    public TypeModel getNestedType() {
        if (nestedType == null) {
            nestedType = ReflectionSignatureModel.of(origin.getComponentType(), this);
        }

        return nestedType;
    }
}
