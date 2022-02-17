package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;
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

    static TypeVariableModel of(@Nonnull AnnotatedTypeVariable origin,
            Model parent) {
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

    static Stream<Class<?>> resolveDependencies(
            AnnotatedTypeVariable signature) {
        // We can resolve only the type variable class bound here (bound class
        // is `dev.hilla.X` in `T extends dev.hilla.X`)
        var bound = signature.getAnnotatedBounds()[0];

        return bound != null ? SignatureModel.resolveDependencies(bound)
                : Stream.empty();
    }

    @Override
    default boolean isTypeVariable() {
        return true;
    }

    SignatureModel resolve();
}
