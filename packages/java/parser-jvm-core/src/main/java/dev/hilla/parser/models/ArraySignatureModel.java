package dev.hilla.parser.models;

import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.ClassInfo;

public interface ArraySignatureModel extends TypeModel {
    static ArraySignatureModel of(@Nonnull ArrayTypeSignature origin,
            @Nonnull Dependable<?, ?> parent) {
        return new ArraySignatureSourceModel(Objects.requireNonNull(origin),
                Objects.requireNonNull(parent));
    }

    static ArraySignatureModel of(@Nonnull Class<?> origin,
            Dependable<?, ?> parent) {
        return new ArraySignatureReflectionModel(Objects.requireNonNull(origin),
                parent);
    }

    static Stream<ClassInfo> resolveDependencies(@Nonnull ArrayTypeSignature signature) {
        return SourceSignatureModel.resolve(
                Objects.requireNonNull(signature).getElementTypeSignature());
    }

    static Stream<Class<?>> resolveDependencies(@Nonnull Class<?> signature) {
        return ReflectionSignatureModel.resolve(signature.getComponentType());
    }

    TypeModel getNestedType();

    @Override
    default boolean isArray() {
        return true;
    }
}
