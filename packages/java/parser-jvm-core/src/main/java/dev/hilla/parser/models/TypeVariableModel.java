package dev.hilla.parser.models;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.TypeVariableSignature;

public interface TypeVariableModel extends SignatureModel {
    static TypeVariableModel of(@Nonnull TypeVariableSignature origin,
            @Nonnull Model parent) {
        return new TypeVariableSourceModel(Objects.requireNonNull(origin),
                Objects.requireNonNull(parent));
    }

    static TypeVariableModel of(@Nonnull TypeVariable<?> origin, Model parent) {
        return new TypeVariableReflectionModel(Objects.requireNonNull(origin),
                parent);
    }

    static Stream<ClassInfo> resolveDependencies(
            TypeVariableSignature signature) {
        // We can resolve only the type variable class bound here (bound class
        // is `dev.hilla.X` in `T extends dev.hilla.X` / `T super dev.hilla.X`)
        var bound = signature.resolve().getClassBound();

        return bound != null ? SignatureModel.resolveDependencies(bound)
                : Stream.empty();
    }

    static Stream<Type> resolveDependencies(TypeVariable<?> signature) {
        // We can resolve only the type variable class bound here (bound class
        // is `dev.hilla.X` in `T extends dev.hilla.X`)
        var bound = signature.getBounds()[0];

        return bound != null
                ? SignatureModel.resolveDependencies(bound)
                : Stream.empty();
    }

    @Override
    default boolean isTypeVariable() {
        return true;
    }

    SignatureModel resolve();
}
